package kr.mmv.mjusugangsincheonghelper.statistic.service;

import kr.mmv.mjusugangsincheonghelper.global.entity.SectionStat;
import kr.mmv.mjusugangsincheonghelper.global.repository.SectionRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.SectionStatRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.SubscriptionRepository;
import kr.mmv.mjusugangsincheonghelper.statistic.dto.SectionStatResponseDto;
import kr.mmv.mjusugangsincheonghelper.statistic.dto.SummaryStatsResponseDto;
import kr.mmv.mjusugangsincheonghelper.statistic.dto.CourseStatisticResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final SectionStatRepository sectionStatRepository;
    private final SectionRepository sectionRepository;
    private final SubscriptionRepository subscriptionRepository;

    /**
     * 전체 구독 통계 조회 (Global Snapshot)
     * - 개별 과목 조회 로직 없음. 무조건 전체 반환.
     * - value: "stats" (TTL 19초 - StatisticCacheConfig 에서 설정)
     * - key: "'all'" (모든 사용자가 동일한 데이터를 보므로 고정 키 사용)
     * - sync: true (DB 보호를 위한 동기화, Thundering Herd 방지)
     * 
     * 동작 방식:
     * 1. Cache Miss 발생 (19초 만료)
     * 2. sync=true 에 의해 단 하나의 스레드만 진입
     * 3. refreshStatistics() 실행 -> DB Native Query 로 전체 과목 집계 및 Max 갱신 (Upsert)
     * 4. findAll() 실행 -> 갱신된 최신 데이터 조회
     * 5. Map 변환 및 반환 -> Redis 캐시에 저장
     */
    @Override
    @Transactional
    @Cacheable(value = "mju:stats", key = "'all'", cacheManager = "cacheManager", sync = true)
    public Map<String, Object> getSubscriptionStats() {

        // 1. [Calculation & Persistence] DB에서 전체 집계 및 갱신 (Upsert)
        // 대량의 데이터를 Java 메모리로 로딩하지 않고 DB 엔진 내에서 처리하여 성능 확보
        sectionStatRepository.refreshStatistics();

        // 2. [Read] 갱신된 데이터 조회
        List<SectionStat> stats = sectionStatRepository.findAll();

        // 3. [Mapping] 프론트엔드에서 O(1)로 찾기 편하게 Map으로 변환
        Map<String, Object> response = new HashMap<>();
        for (SectionStat stat : stats) {
            response.put(stat.getSectionCls(), SectionStatResponseDto.from(stat));
        }

        // 4. 전체 데이터 갱신 시점 추가 (Unix Timestamp)
        response.put("updated_at", System.currentTimeMillis() / 1000);

        return response;
    }

    /**
     * 홈페이지 통계 요약 조회
     * - 공지사항 형식으로 서비스 현황을 표시
     * - TTL 19초 캐싱 적용
     */
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mju:stats", key = "'summary'", cacheManager = "cacheManager", sync = true)
    public SummaryStatsResponseDto getSummaryStats() {
        // 1. 총 과목 개수 (활성화된 강의)
        long totalSections = sectionRepository.countByIsActiveTrue();

        // 2. 총 구독자 수 (구독을 1개 이상 가진 고유 학생 수)
        long totalSubscribers = subscriptionRepository.countDistinctSubscribers();

        // 3. 정원 마감 과목 개수
        long fullSections = sectionRepository.countByIsFullTrueAndIsActiveTrue();

        // 4. 학과별 구독자 수 TOP 10
        List<Object[]> departmentStats = subscriptionRepository.countByDepartmentTop10();
        List<SummaryStatsResponseDto.DepartmentSubscription> topDepartments = departmentStats.stream()
                .limit(10)
                .map(row -> SummaryStatsResponseDto.DepartmentSubscription.builder()
                        .departmentName((String) row[0])
                        .subscriberCount((Long) row[1])
                        .build())
                .collect(Collectors.toList());

        return SummaryStatsResponseDto.builder()
                .totalSections(totalSections)
                .totalSubscribers(totalSubscribers)
                .fullSections(fullSections)
                .totalSectionsForFullRatio(totalSections)
                .topDepartments(topDepartments)
                .updatedAt(System.currentTimeMillis() / 1000)
                .build();
    }
    @Override
    @Transactional(readOnly = true)
    @Cacheable(value = "mju:stats", key = "'course:' + #sectioncls", cacheManager = "cacheManager", sync = true)
    public CourseStatisticResponseDto getCourseStatistics(String sectioncls) {
        // 1. 총 구독자 수
        long totalCount = subscriptionRepository.countBySectionSectioncls(sectioncls);

        // 2. 학년별 통계
        List<Object[]> gradeStats = subscriptionRepository.countBySectionSectionclsGroupByStudentGrade(sectioncls);
        Map<String, Long> gradeCounts = new HashMap<>();
        for (Object[] row : gradeStats) {
            String grade = (String) row[0];
            Long count = (Long) row[1];
            gradeCounts.put(grade, count);
        }

        // 3. 학과별 통계
        List<Object[]> deptStats = subscriptionRepository.countBySectionSectionclsGroupByStudentDepartment(sectioncls);
        Map<String, Long> deptCounts = new HashMap<>();
        for (Object[] row : deptStats) {
            String dept = (String) row[0];
            Long count = (Long) row[1];
            deptCounts.put(dept, count);
        }

        return CourseStatisticResponseDto.of(sectioncls, totalCount, gradeCounts, deptCounts);
    }
}

