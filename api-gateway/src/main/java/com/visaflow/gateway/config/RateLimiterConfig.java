package com.visaflow.gateway.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver userKeyResolver() {
        return exchange -> {
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            // If user is authenticated use their ID, else use client IP
            if (userId != null && !userId.isEmpty()) {
                return Mono.just(userId);
            }
            String clientIp = exchange.getRequest().getRemoteAddress() != null 
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress() 
                    : "anonymous";
            return Mono.just(clientIp);
        };
    }
}
