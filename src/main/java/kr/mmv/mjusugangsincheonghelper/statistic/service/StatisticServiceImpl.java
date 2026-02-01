package kr.mmv.mjusugangsincheonghelper.statistic.service;

import kr.mmv.mjusugangsincheonghelper.global.entity.SectionStat;
import kr.mmv.mjusugangsincheonghelper.global.repository.SectionStatRepository;
import kr.mmv.mjusugangsincheonghelper.statistic.dto.SectionStatResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StatisticServiceImpl implements StatisticService {

    private final SectionStatRepository sectionStatRepository;

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
    @Cacheable(value = "mju:stats", key = "'all'", cacheManager = "statisticCacheManager", sync = true)
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
}
