package com.RQ.tuyunthinktank.common;

import com.github.benmanes.caffeine.cache.Cache;
import com.RQ.tuyunthinktank.config.TuyunProperties;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class LocalCacheManager {
    private final Cache<String, String> localCache;

    public LocalCacheManager(TuyunProperties tuyunProperties) {
        this.localCache = Caffeine.newBuilder()
                .maximumSize(tuyunProperties.getCache().getLocalMaxSize())
                .expireAfterWrite(tuyunProperties.getCache().getLocalTtlMinutes(), TimeUnit.MINUTES)
                .build();
    }

    public String getIfPresent(String key) {
        return localCache.getIfPresent(key);
    }

    public void put(String key, String value) {
        localCache.put(key, value);
    }

    public void invalidate(String key) {
        localCache.invalidate(key);
    }
}