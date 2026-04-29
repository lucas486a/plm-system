package com.plm.config;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;

/**
 * Cache configuration for PLM system.
 * Uses Spring's built-in ConcurrentMapCache for in-memory caching.
 * Caches are configured for frequently accessed entities:
 * - parts: Part lookups by ID and part number
 * - documents: Document lookups by ID and document number
 * - boms: BOM lookups by ID and assembly
 * - bom-items: BOM item lookups by BOM ID
 * - assemblies: Assembly lookups by part number
 */
@Configuration
@EnableCaching
public class CacheConfig {

    public static final String PARTS_CACHE = "parts";
    public static final String DOCUMENTS_CACHE = "documents";
    public static final String BOMS_CACHE = "boms";
    public static final String BOM_ITEMS_CACHE = "bom-items";
    public static final String ASSEMBLIES_CACHE = "assemblies";

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();
        cacheManager.setCaches(Arrays.asList(
                new ConcurrentMapCache(PARTS_CACHE),
                new ConcurrentMapCache(DOCUMENTS_CACHE),
                new ConcurrentMapCache(BOMS_CACHE),
                new ConcurrentMapCache(BOM_ITEMS_CACHE),
                new ConcurrentMapCache(ASSEMBLIES_CACHE)
        ));
        return cacheManager;
    }
}
