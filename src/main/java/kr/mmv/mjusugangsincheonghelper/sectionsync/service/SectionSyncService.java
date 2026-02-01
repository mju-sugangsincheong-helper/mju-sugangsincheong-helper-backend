package kr.mmv.mjusugangsincheonghelper.sectionsync.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.mmv.mjusugangsincheonghelper.global.entity.Section;
import kr.mmv.mjusugangsincheonghelper.global.entity.Subscription;
import kr.mmv.mjusugangsincheonghelper.global.repository.SectionRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.StudentDeviceRepository;
import kr.mmv.mjusugangsincheonghelper.global.repository.SubscriptionRepository;
import kr.mmv.mjusugangsincheonghelper.notification.dto.NotificationMessageDto;
import kr.mmv.mjusugangsincheonghelper.sectionsync.dto.SectionSyncDataDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 강의 동기화 서비스
 * Redis에서 크롤러 데이터를 읽어 DB와 동기화
 * 
 * 핵심 로직:
 * 1. prev == null && DB empty → 초기화 모드 (Bulk Insert, 알림 X)
 * 2. prev == null && DB exists → 복구 모드 (prev만 갱신)
 * 3. prev != null → 비교 모드 (Diff → DB 업데이트 + 알림)
 * 
 * PK: sectioncls (coursecls in JSON)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SectionSyncService {

    private final SectionRepository sectionRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final StudentDeviceRepository studentDeviceRepository;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String REDIS_KEY_CURR = "mju:section:curr";
    private static final String REDIS_KEY_PREV = "mju:section:prev";
    private static final String NOTIFICATION_QUEUE = "mju:section:notification:queue";
    private static final int BATCH_SIZE = 400;

    /**
     * 강의 동기화 메인 로직
     */
    @Transactional
    public void syncSections() {
        log.info("Starting section sync...");
        
        String currJson = redisTemplate.opsForValue().get(REDIS_KEY_CURR);
        String prevJson = redisTemplate.opsForValue().get(REDIS_KEY_PREV);
        
        if (currJson == null || currJson.isBlank()) {
            log.warn("No current section data in Redis. Skipping sync.");
            return;
        }
        
        // 현재 데이터를 이전 데이터로 저장 (다음 비교를 위해)
        redisTemplate.opsForValue().set(REDIS_KEY_PREV, currJson);
        
        List<SectionSyncDataDto> currSections = parseJson(currJson);
        if (currSections.isEmpty()) {
            log.warn("Empty section list from Redis. Skipping sync.");
            return;
        }
        
        if (prevJson == null || prevJson.isBlank()) {
            // Case 1 or 2: prev가 없음
            handleNoPrevData(currSections);
        } else {
            // Case 3: 정상 비교 모드
            List<SectionSyncDataDto> prevSections = parseJson(prevJson);
            handleCompareMode(currSections, prevSections);
        }
        
        log.info("Section sync completed.");
    }

    /**
     * Case 1 & 2: 이전 데이터가 없는 경우
     */
    private void handleNoPrevData(List<SectionSyncDataDto> currSections) {
        long dbCount = sectionRepository.count();
        
        if (dbCount == 0) {
            // Case 1: 초기화 모드 - DB가 비어있음
            log.info("Init mode: DB is empty. Performing bulk insert without notifications.");
            bulkInsertSections(currSections, false);
        } else {
            // Case 2: 복구 모드 - Redis만 비어있었음
            log.info("Recovery mode: DB has {} records. Updating sections.", dbCount);
            bulkInsertSections(currSections, false);
        }
    }

    /**
     * Case 3: 정상 비교 모드
     */
    private void handleCompareMode(List<SectionSyncDataDto> currSections, List<SectionSyncDataDto> prevSections) {
        // Map으로 변환 (Key: sectioncls = coursecls)
        Map<String, SectionSyncDataDto> currMap = currSections.stream()
                .filter(s -> s.getCoursecls() != null)
                .collect(Collectors.toMap(SectionSyncDataDto::getUniqueKey, s -> s, (a, b) -> a));
        Map<String, SectionSyncDataDto> prevMap = prevSections.stream()
                .filter(s -> s.getCoursecls() != null)
                .collect(Collectors.toMap(SectionSyncDataDto::getUniqueKey, s -> s, (a, b) -> a));
        
        List<Section> toInsert = new ArrayList<>();
        List<Section> toUpdate = new ArrayList<>();
        List<String> vacancyOccurredSectionIds = new ArrayList<>();
        
        // 현재 데이터 처리
        for (SectionSyncDataDto curr : currSections) {
            if (curr.getCoursecls() == null) continue;
            
            String key = curr.getUniqueKey();
            SectionSyncDataDto prev = prevMap.get(key);
            
            if (prev == null) {
                // 신규 강의
                toInsert.add(toEntity(curr));
            } else {
                // 기존 강의 - 변경 확인
                Optional<Section> existingOpt = sectionRepository.findById(key);
                
                if (existingOpt.isPresent()) {
                    Section existing = existingOpt.get();
                    boolean wasFull = Boolean.TRUE.equals(existing.getIsFull());
                    
                    updateSectionFields(existing, curr);
                    existing.updateFullStatus();
                    
                    toUpdate.add(existing);
                    
                    // 만석 → 여석 변경 감지
                    if (wasFull && !Boolean.TRUE.equals(existing.getIsFull())) {
                        log.info("Vacancy occurred: {} - {}", existing.getCurinm(), existing.getSectioncls());
                        vacancyOccurredSectionIds.add(existing.getSectioncls());
                    }
                } else {
                    // DB에는 없지만 prev에 있던 경우 (신규 추가)
                    toInsert.add(toEntity(curr));
                }
            }
        }
        
        // 폐강 처리 (curr에 없고 prev에 있는 경우)
        Set<String> currKeys = currMap.keySet();
        for (String prevKey : prevMap.keySet()) {
            if (!currKeys.contains(prevKey)) {
                sectionRepository.findById(prevKey)
                        .ifPresent(section -> {
                            log.info("Section deactivated: {} - {}", section.getCurinm(), section.getSectioncls());
                            section.deactivate();
                            toUpdate.add(section);
                        });
            }
        }
        
        // DB 저장
        if (!toInsert.isEmpty()) {
            sectionRepository.saveAll(toInsert);
            log.info("Inserted {} new sections", toInsert.size());
        }
        if (!toUpdate.isEmpty()) {
            sectionRepository.saveAll(toUpdate);
            log.info("Updated {} sections", toUpdate.size());
        }
        
        // 여석 알림 발송
        if (!vacancyOccurredSectionIds.isEmpty()) {
            sendVacancyNotifications(vacancyOccurredSectionIds);
        }
    }

    /**
     * Bulk Insert (초기화/복구 모드)
     */
    private void bulkInsertSections(List<SectionSyncDataDto> sections, boolean sendNotifications) {
        // 기존 데이터의 sectioncls Set
        Map<String, Section> existingMap = sectionRepository.findAll().stream()
                .collect(Collectors.toMap(Section::getSectioncls, s -> s, (a, b) -> a));
        
        List<Section> toSave = new ArrayList<>();
        
        for (SectionSyncDataDto dto : sections) {
            if (dto.getCoursecls() == null) continue;
            
            String key = dto.getUniqueKey();
            Section existing = existingMap.get(key);
            
            if (existing != null) {
                // 업데이트
                updateSectionFields(existing, dto);
                existing.updateFullStatus();
                existing.setIsActive(true);
                existing.setDeactivatedAt(null);
                toSave.add(existing);
            } else {
                // 신규
                toSave.add(toEntity(dto));
            }
        }
        
        sectionRepository.saveAll(toSave);
        log.info("Bulk processed {} sections", toSave.size());
    }

    /**
     * 여석 알림 발송 (Redis Queue Push)
     */
    private void sendVacancyNotifications(List<String> sectionIds) {
        List<Subscription> subscriptions = subscriptionRepository.findSubscribersForSectionIds(sectionIds);
        
        if (subscriptions.isEmpty()) {
            log.info("No subscribers for vacancy notifications");
            return;
        }
        
        log.info("Processing vacancy notifications for {} subscribers", subscriptions.size());

        // 강의별로 그룹화
        Map<Section, List<Subscription>> groupedBySections = subscriptions.stream()
                .collect(Collectors.groupingBy(Subscription::getSection));

        for (Map.Entry<Section, List<Subscription>> entry : groupedBySections.entrySet()) {
            Section section = entry.getKey();
            List<Subscription> sectionSubscriptions = entry.getValue();

            // 구독자들의 학생 ID 수집
            List<String> studentIds = sectionSubscriptions.stream()
                    .map(s -> s.getUser().getStudentId())
                    .collect(Collectors.toList());

            // StudentDevice 테이블에서 FCM 토큰 조회
            List<String> fcmTokens = studentDeviceRepository.findFcmTokensByStudentIds(studentIds);

            // 이메일 수집
            List<String> emails = sectionSubscriptions.stream()
                    .map(s -> s.getUser().getEmail())
                    .filter(email -> email != null && !email.isBlank())
                    .collect(Collectors.toList());

            // 배치로 나눠서 Queue에 전송
            pushToNotificationQueue(section, fcmTokens, emails);
        }
    }

    /**
     * 배치 알림 전송 (Push to Redis)
     */
    private void pushToNotificationQueue(Section section, List<String> fcmTokens, List<String> emails) {
        int vacancy = section.getAvailableSeats();

        // FCM 토큰을 배치로 나눔
        for (int i = 0; i < fcmTokens.size(); i += BATCH_SIZE) {
            List<String> batchTokens = fcmTokens.subList(i, Math.min(i + BATCH_SIZE, fcmTokens.size()));
            List<String> batchEmails = i < emails.size() 
                    ? emails.subList(i, Math.min(i + BATCH_SIZE, emails.size()))
                    : new ArrayList<>();

            NotificationMessageDto message = NotificationMessageDto.builder()
                    .sectioncls(section.getSectioncls())
                    .curinm(section.getCurinm())
                    .profnm(section.getProfnm())
                    .deptcd(section.getDeptcd())
                    .deptnm(section.getDeptnm())
                    .vacancy(vacancy)
                    .fcmTokens(batchTokens)
                    .emails(batchEmails)
                    .build();

            try {
                String json = objectMapper.writeValueAsString(message);
                redisTemplate.opsForList().leftPush(NOTIFICATION_QUEUE, json);
            } catch (Exception e) {
                log.error("Failed to push notification to queue", e);
            }
        }

        log.info("Queued vacancy notifications for section: {} ({} tokens, {} emails)", 
                section.getCurinm(), fcmTokens.size(), emails.size());
    }

    /**
     * JSON 파싱
     */
    private List<SectionSyncDataDto> parseJson(String json) {
        try {
            return objectMapper.readValue(json, new TypeReference<List<SectionSyncDataDto>>() {});
        } catch (Exception e) {
            log.error("Failed to parse section JSON", e);
            return Collections.emptyList();
        }
    }

    /**
     * DTO → Entity 변환
     * JSON 원본 필드 그대로 매핑
     */
    private Section toEntity(SectionSyncDataDto dto) {
        return Section.builder()
                .sectioncls(dto.getCoursecls())
                .curiyear(dto.getCuriyear())
                .curinum(dto.getCurinum())
                .curinm(dto.getCurinm())
                .curinum2(dto.getCurinum2())
                .groupcd(dto.getGroupcd())
                .lecttime(dto.getLecttime())
                .cdtnum(dto.getCdtnumInt())
                .cdttime(dto.getCdttimeInt())
                .lecperiod(dto.getLecperiod())
                .deptnm(dto.getDeptnm())
                .deptcd(dto.getDeptcd())
                .profnm(dto.getProfnm())
                .profid(dto.getProfid())
                .campusdiv(dto.getCampusdiv())
                .classtype(dto.getClasstype())
                .comyear(dto.getComyearInt())
                .bagcnt(dto.getBagcntInt())
                .takelim(dto.getTakelimInt())
                .listennow(dto.getListennowInt())
                .isFull(dto.isFull())
                .isActive(true)
                .build();
    }

    /**
     * Entity 필드 업데이트
     * 변경 가능한 필드만 업데이트 (PK와 기본 식별 정보 제외)
     */
    private void updateSectionFields(Section section, SectionSyncDataDto dto) {
        // 기본 정보 업데이트
        section.setCurinm(dto.getCurinm());
        section.setLecttime(dto.getLecttime());
        section.setCdtnum(dto.getCdtnumInt());
        section.setCdttime(dto.getCdttimeInt());
        section.setLecperiod(dto.getLecperiod());
        
        // 교수/학과 정보 업데이트
        section.setDeptnm(dto.getDeptnm());
        section.setProfnm(dto.getProfnm());
        section.setProfid(dto.getProfid());
        
        // 분류/통계 정보 업데이트
        section.setClasstype(dto.getClasstype());
        section.setBagcnt(dto.getBagcntInt());
        
        // 상태 정보 업데이트
        section.setTakelim(dto.getTakelimInt());
        section.setListennow(dto.getListennowInt());
    }
}
