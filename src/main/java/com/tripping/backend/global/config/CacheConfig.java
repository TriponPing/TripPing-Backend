package com.tripping.backend.global.config;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

// DataLabApiService.fetchDailyVisitorAggregates() 캐싱 설정.
// 지역 하나 조회할 때마다 관광공사 API에서 전국 1년치(약 17,000건)를 매번 새로 받아오면
// 요청당 5초 가까이 걸린다. 같은 (areaCd, start, end) 조합은 캐시해서 재사용한다.
//
// Caffeine 기반 CacheManager를 쓰려고 했으나, 이 프로젝트의 Spring Boot 버전에서
// org.springframework.cache.caffeine(spring-context-support 모듈) 관련 컴파일 에러가 나서
// 별도 의존성이 필요 없는 기본 ConcurrentMapCacheManager로 대체했다 (spring-context에 이미
// 포함돼 있어 build.gradle 수정 불필요). 이건 TTL을 자체 지원하지 않으므로, 매일 자정에
// 캐시를 통째로 비우는 스케줄러로 "하루 캐시"를 흉내낸다. DataLab 원본 데이터 갱신 주기가
// 하루보다 훨씬 길어서(약 5주 지연 공개) 이 정도로 충분하다.
@Configuration
@EnableCaching
@EnableScheduling
public class CacheConfig {

    public static final String DATA_LAB_DAILY_VISITORS_CACHE = "dataLabDailyVisitors";

    private final ConcurrentMapCacheManager mapCacheManager =
            new ConcurrentMapCacheManager(DATA_LAB_DAILY_VISITORS_CACHE);

    @Bean
    public CacheManager cacheManager() {
        return mapCacheManager;
    }

    // 매일 자정(00:00)에 캐시 전체 삭제 — ConcurrentMapCacheManager는 TTL이 없어서
    // 이렇게 하루 단위로 강제로 비워준다.
    @Scheduled(cron = "0 0 0 * * *")
    public void evictDailyVisitorsCache() {
        Cache cache = mapCacheManager.getCache(DATA_LAB_DAILY_VISITORS_CACHE);
        if (cache != null) {
            cache.clear();
        }
    }
}
