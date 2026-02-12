package kr.mmv.mjusugangsincheonghelper.practice.service;

import kr.mmv.mjusugangsincheonghelper.global.api.code.ErrorCode;
import kr.mmv.mjusugangsincheonghelper.global.api.exception.BaseException;
import kr.mmv.mjusugangsincheonghelper.global.entity.PracticeSession;
import kr.mmv.mjusugangsincheonghelper.global.entity.Student;
import kr.mmv.mjusugangsincheonghelper.global.repository.PracticeSessionRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.StudentRepository;
import kr.mmv.mjusugangsincheonghelper.practice.dto.SubmitPracticeRequestDto;
import kr.mmv.mjusugangsincheonghelper.practice.dto.PracticeRecordResponseDto;
import kr.mmv.mjusugangsincheonghelper.practice.dto.PracticeRankResponseDto;
import kr.mmv.mjusugangsincheonghelper.practice.dto.PracticeRankResponseDto.RankData;
import kr.mmv.mjusugangsincheonghelper.practice.dto.PracticeRankResponseDto.RankEntry;
import kr.mmv.mjusugangsincheonghelper.practice.dto.PracticeRecordResponseDto.MyRecord;
import kr.mmv.mjusugangsincheonghelper.practice.dto.DepartmentRankResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PracticeService {

    private final PracticeSessionRepository practiceSessionRepository;
    private final StudentRepository studentRepository;

    private static final long MIN_TIME_PER_COURSE_MS = 10;
    private static final int MAX_BASKET_COUNT = 10;

    @Transactional
    public void submitPractice(String studentId, SubmitPracticeRequestDto request) {
        // 1. 장바구니 개수 검증
        if (request.getCountNum() > MAX_BASKET_COUNT) {
            throw new BaseException(ErrorCode.PRACTICE_BASKET_LIMIT_EXCEEDED);
        }

        // 2. 최소 시간 검증 (과목당 0.01초)
        long minTotalTime = request.getCountNum() * MIN_TIME_PER_COURSE_MS;
        if (request.getTotalTimeMs() < minTotalTime) {
            log.warn("Macro suspected: student={}, count={}, time={}", studentId, request.getCountNum(), request.getTotalTimeMs());
            throw new BaseException(ErrorCode.PRACTICE_TIME_TOO_SHORT);
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new BaseException(ErrorCode.AUTH_USER_NOT_FOUND));

        PracticeSession session = PracticeSession.builder()
                .student(student)
                .countNum(request.getCountNum())
                .timeMs(request.getTotalTimeMs())
                .build();

        practiceSessionRepository.save(session);
        log.info("Practice session submitted for student {}: count={}, time={}", studentId, request.getCountNum(), request.getTotalTimeMs());
    }

    @Cacheable(value = "ranking", key = "'global'", sync = true)
    public PracticeRankResponseDto getGlobalRanking() {
        List<PracticeSession> allBestRecords = practiceSessionRepository.findGlobalRanking();
        
        Map<String, RankData> data = new HashMap<>();
        
        // count_num 별로 그룹핑
        Map<Integer, List<PracticeSession>> byCountNum = allBestRecords.stream()
                .collect(Collectors.groupingBy(PracticeSession::getCountNum));

        byCountNum.forEach((countNum, sessions) -> {
            sessions.sort(Comparator.comparingLong(PracticeSession::getTimeMs));

            List<RankEntry> totalRanks = new ArrayList<>();
            Map<String, List<RankEntry>> deptRanks = new HashMap<>();
            Map<String, List<RankEntry>> gradeRanks = new HashMap<>();

            int rank = 1;
            for (PracticeSession s : sessions) {
                Student st = s.getStudent();
                RankEntry entry = RankEntry.builder()
                        .rank(rank++) 
                        .name(maskName(st.getName()))
                        .dept(st.getDepartment())
                        .grade(st.getGrade())
                        .time(s.getTimeMs())
                        .build();

                totalRanks.add(entry);

                deptRanks.computeIfAbsent(st.getDepartment(), k -> new ArrayList<>()).add(entry);
                gradeRanks.computeIfAbsent(st.getGrade(), k -> new ArrayList<>()).add(entry);
            }
            
            Map<String, List<RankEntry>> deptRanksRecalculated = recalculateGroupRank(deptRanks);
            Map<String, List<RankEntry>> gradeRanksRecalculated = recalculateGroupRank(gradeRanks);
            
            if (totalRanks.size() > 100) totalRanks = new ArrayList<>(totalRanks.subList(0, 100));

            data.put("count_" + countNum, RankData.builder()
                    .total(totalRanks)
                    .dept(deptRanksRecalculated)
                    .grade(gradeRanksRecalculated)
                    .build());
        });

        return PracticeRankResponseDto.builder()
                .updatedAt(Instant.now().toEpochMilli())
                .data(data)
                .build();
    }



    @Cacheable(value = "ranking", key = "'user:' + #studentId", sync = true)
    public PracticeRecordResponseDto getMyPracticeRecords(String studentId) {
        // 1. 내 최고 기록 조회 (DB) - 각 장바구니 개수별 최고 기록
        List<PracticeSession> myBestRecords = practiceSessionRepository.findBestRecordsByStudent(studentId);
        
        if (myBestRecords.isEmpty()) {
             return PracticeRecordResponseDto.builder()
                    .updatedAt(Instant.now().toEpochMilli())
                    .studentId(studentId)
                    .records(Collections.emptyMap())
                    .build();
        }
        
        // 내 학과, 학년 정보 (모든 기록에서 동일하므로 첫 번째에서 추출)
        Student student = myBestRecords.get(0).getStudent();
        String myDept = student.getDepartment();
        String myGrade = student.getGrade();
        
        Map<String, MyRecord> records = new HashMap<>();
        
        for (PracticeSession myRecord : myBestRecords) {
            Integer countNum = myRecord.getCountNum();
            Long myTime = myRecord.getTimeMs();
            String key = "count_" + countNum;

            // 2. 정확한 랭킹 계산 (DB Count 쿼리)
            // 나보다 기록이 좋은 사람 수 + 1 = 내 등수
            long higherRankTotal = practiceSessionRepository.countHigherRank(countNum, myTime);
            long higherRankDept = practiceSessionRepository.countHigherRankByDept(countNum, myTime, myDept);
            long higherRankGrade = practiceSessionRepository.countHigherRankByGrade(countNum, myTime, myGrade);

            records.put(key, MyRecord.builder()
                    .bestTimeMs(myTime)
                    .totalRank((int) higherRankTotal + 1) 
                    .deptRank((int) higherRankDept + 1)
                    .gradeRank((int) higherRankGrade + 1)
                    .build());
        }

        return PracticeRecordResponseDto.builder()
                .updatedAt(Instant.now().toEpochMilli())
                .studentId(studentId)
                .records(records)
                .build();
    }

    private String maskName(String name) {
        if (name == null || name.length() < 2) return name;
        
        if (name.length() == 2) {
            return "*" + name.substring(1); // "김철" -> "*철"
        }
        
        // 3자 이상: 앞 2자리 마스킹 ("홍길동" -> "**동", "제갈공명" -> "**공명")
        return "**" + name.substring(2); 
    }

    @Cacheable(value = "ranking", key = "'dept_summary'", sync = true)
    public DepartmentRankResponseDto getDepartmentRankingSummary() {
        // 1. 각 학생의 과목 수별 최고 기록 가져오기
        List<PracticeSession> allBestRecords = practiceSessionRepository.findGlobalRanking();

        // TreeMap을 사용하여 count_N 순서대로 정렬 (key: count_1, count_2, ...)
        Map<String, List<DepartmentRankResponseDto.RankEntry>> rankingMap = new TreeMap<>(
                Comparator.comparingInt(s -> Integer.parseInt(s.replace("count_", "")))
        );

        // 2. 과목 수(countNum) 별로 그룹핑
        Map<Integer, List<PracticeSession>> byCountNum = allBestRecords.stream()
                .collect(Collectors.groupingBy(PracticeSession::getCountNum));

        byCountNum.forEach((countNum, sessions) -> {
            // 3. 해당 과목 수 내에서 학과별 최고 기록 찾기
            Map<String, PracticeSession> deptBestRecord = new HashMap<>();
            for (PracticeSession s : sessions) {
                String dept = s.getStudent().getDepartment();
                if (!deptBestRecord.containsKey(dept) || s.getTimeMs() < deptBestRecord.get(dept).getTimeMs()) {
                    deptBestRecord.put(dept, s);
                }
            }

            // 4. 학과별 최고 기록들을 시간순으로 정렬 (상위 5개)
            List<PracticeSession> sortedDepts = deptBestRecord.values().stream()
                    .sorted(Comparator.comparingLong(PracticeSession::getTimeMs))
                    .limit(5)
                    .collect(Collectors.toList());

            // 5. RankEntry 생성
            List<DepartmentRankResponseDto.RankEntry> entries = new ArrayList<>();
            for (int i = 0; i < sortedDepts.size(); i++) {
                PracticeSession s = sortedDepts.get(i);
                Student student = s.getStudent();
                entries.add(DepartmentRankResponseDto.RankEntry.builder()
                        .rank(i + 1)
                        .dept(student.getDepartment())
                        .bestRecord(DepartmentRankResponseDto.BestRecord.builder()
                                .name(maskName(student.getName()))
                                .grade(student.getGrade())
                                .time(s.getTimeMs())
                                .build())
                        .build());
            }

            rankingMap.put("count_" + countNum, entries);
        });

        return DepartmentRankResponseDto.builder()
                .updatedAt(Instant.now().toEpochMilli())
                .ranking(rankingMap)
                .build();
    }
    
    private Map<String, List<RankEntry>> recalculateGroupRank(Map<String, List<RankEntry>> original) {
        Map<String, List<RankEntry>> result = new HashMap<>();
        
        original.forEach((key, list) -> {
            List<RankEntry> sorted = new ArrayList<>(list);
            List<RankEntry> newRankedList = new ArrayList<>();
            int r = 1;
            for (RankEntry e : sorted) {
                newRankedList.add(RankEntry.builder()
                        .rank(r++)
                        .name(e.getName())
                        .dept(e.getDept())
                        .grade(e.getGrade())
                        .time(e.getTime())
                        .build());
            }
            if (newRankedList.size() > 100) newRankedList = new ArrayList<>(newRankedList.subList(0, 100));
            result.put(key, newRankedList);
        });
        return result;
    }
}
