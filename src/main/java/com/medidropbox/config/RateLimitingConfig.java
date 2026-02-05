package com.medidropbox.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

/**
 * Rate Limiting Configuration using Bucket4j
 * Configure rate limits for API endpoints
 */
@Configuration
public class RateLimitingConfig {
    
    /**
     * General API rate limit: 100 requests per minute
     */
    @Bean
    public Bucket generalApiBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(100, Refill.intervally(100, Duration.ofMinutes(1))))
                .build();
    }
    
    /**
     * Auth endpoints rate limit: 10 requests per minute
     */
    @Bean
    public Bucket authApiBucket() {
        return Bucket.builder()
                .addLimit(Bandwidth.classic(10, Refill.intervally(10, Duration.ofMinutes(1))))
                .build();
    }
}
