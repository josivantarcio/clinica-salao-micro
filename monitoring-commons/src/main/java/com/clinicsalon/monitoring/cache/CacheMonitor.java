package com.clinicsalon.monitoring.cache;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.Tags;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Monitoramento de estatísticas de cache para todos os microsserviços
 * Coleta métricas de uso de cache, taxa de acertos/erros e tamanho
 */
@Component
@Slf4j
public class CacheMonitor {

    private final CacheManager cacheManager;
    private final MeterRegistry meterRegistry;
    
    // Contadores para estatísticas de cache
    private final Map<String, AtomicLong> cacheHits = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> cacheMisses = new ConcurrentHashMap<>();
    private final Map<String, AtomicLong> cacheEvictions = new ConcurrentHashMap<>();

    public CacheMonitor(CacheManager cacheManager, MeterRegistry meterRegistry) {
        this.cacheManager = cacheManager;
        this.meterRegistry = meterRegistry;
        
        // Inicializar contadores para cada cache
        cacheManager.getCacheNames().forEach(cacheName -> {
            cacheHits.put(cacheName, new AtomicLong(0));
            cacheMisses.put(cacheName, new AtomicLong(0));
            cacheEvictions.put(cacheName, new AtomicLong(0));
            
            // Registrar gauges para cada métrica
            Tags tags = Tags.of(Tag.of("cache", cacheName));
            meterRegistry.gauge("cache.size", tags, cacheName, n -> getCacheSize(n));
            meterRegistry.gauge("cache.hits", tags, cacheHits.get(cacheName), AtomicLong::get);
            meterRegistry.gauge("cache.misses", tags, cacheMisses.get(cacheName), AtomicLong::get);
            meterRegistry.gauge("cache.evictions", tags, cacheEvictions.get(cacheName), AtomicLong::get);
        });
    }
    
    /**
     * Registra um acerto no cache
     */
    public void recordCacheHit(String cacheName) {
        AtomicLong counter = cacheHits.computeIfAbsent(cacheName, k -> new AtomicLong(0));
        counter.incrementAndGet();
        
        // Registrar evento na métrica
        meterRegistry.counter("cache.hit.count", "cache", cacheName).increment();
    }
    
    /**
     * Registra uma falha no cache
     */
    public void recordCacheMiss(String cacheName) {
        AtomicLong counter = cacheMisses.computeIfAbsent(cacheName, k -> new AtomicLong(0));
        counter.incrementAndGet();
        
        // Registrar evento na métrica
        meterRegistry.counter("cache.miss.count", "cache", cacheName).increment();
    }
    
    /**
     * Registra uma evicção no cache
     */
    public void recordCacheEviction(String cacheName) {
        AtomicLong counter = cacheEvictions.computeIfAbsent(cacheName, k -> new AtomicLong(0));
        counter.incrementAndGet();
        
        // Registrar evento na métrica
        meterRegistry.counter("cache.eviction.count", "cache", cacheName).increment();
    }
    
    /**
     * Obtém o tamanho estimado do cache
     */
    private long getCacheSize(String cacheName) {
        Cache cache = cacheManager.getCache(cacheName);
        if (cache instanceof com.github.benmanes.caffeine.cache.Cache) {
            return ((com.github.benmanes.caffeine.cache.Cache<?, ?>) cache).estimatedSize();
        }
        return -1; // Tamanho desconhecido
    }
    
    /**
     * Registra estatísticas de todos os caches a cada minuto
     */
    @Scheduled(fixedRate = 60000)
    public void logCacheStatistics() {
        Map<String, Map<String, Object>> stats = new HashMap<>();
        
        cacheManager.getCacheNames().forEach(cacheName -> {
            Map<String, Object> cacheStats = new HashMap<>();
            cacheStats.put("size", getCacheSize(cacheName));
            cacheStats.put("hits", cacheHits.getOrDefault(cacheName, new AtomicLong(0)).get());
            cacheStats.put("misses", cacheMisses.getOrDefault(cacheName, new AtomicLong(0)).get());
            cacheStats.put("evictions", cacheEvictions.getOrDefault(cacheName, new AtomicLong(0)).get());
            
            // Calcular taxa de acertos
            long hits = cacheHits.getOrDefault(cacheName, new AtomicLong(0)).get();
            long misses = cacheMisses.getOrDefault(cacheName, new AtomicLong(0)).get();
            double hitRatio = hits + misses > 0 ? (double) hits / (hits + misses) : 0.0;
            cacheStats.put("hitRatio", String.format("%.2f", hitRatio * 100) + "%");
            
            stats.put(cacheName, cacheStats);
        });
        
        log.info("Cache Statistics: {}", stats);
    }
}
