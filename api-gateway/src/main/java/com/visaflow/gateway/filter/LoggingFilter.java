package com.visaflow.gateway.filter;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Slf4j
@Component
public class LoggingFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String path = exchange.getRequest().getURI().getPath();

        return chain.filter(exchange).then(Mono.fromRunnable(() -> {
            long responseTime = System.currentTimeMillis() - startTime;
            int statusCode = exchange.getResponse().getStatusCode() != null 
                    ? exchange.getResponse().getStatusCode().value() : 200;
            String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
            
            log.info("Request Path: {} | User ID: {} | Response Time: {}ms | Status Code: {}",
                    path, userId == null ? "Anonymous" : userId, responseTime, statusCode);
        }));
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
