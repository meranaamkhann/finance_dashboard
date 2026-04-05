package com.finance.dashboard.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableCaching
public class CacheConfig {

    public static final String CACHE_DASHBOARD    = "dashboardSummary";
    public static final String CACHE_HEALTH_SCORE = "healthScore";
    public static final String CACHE_CATEGORY     = "categoryTotals";

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager(
                CACHE_DASHBOARD,
                CACHE_HEALTH_SCORE,
                CACHE_CATEGORY
        );
    }
}
