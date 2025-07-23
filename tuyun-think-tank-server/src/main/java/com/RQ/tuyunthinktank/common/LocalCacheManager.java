package com.RQ.tuyunthinktank.common;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

    @Component
    public class LocalCacheManager {
        private final Cache<String, String> localCache;

        public LocalCacheManager() {
            this.localCache = Caffeine.newBuilder()
                    .maximumSize(1000)
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
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