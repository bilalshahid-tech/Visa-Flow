package com.visaflow.gateway.filter;

import com.visaflow.gateway.util.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Predicate;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Autowired
    private JwtUtil jwtUtil;

    private final List<String> openApiEndpoints = List.of(
            "/api/auth/login",
            "/api/auth/register",
            "/eureka"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();

        Predicate<ServerHttpRequest> isApiSecured = r -> openApiEndpoints.stream()
                .noneMatch(uri -> r.getURI().getPath().contains(uri));

        if (isApiSecured.test(request)) {
            if (!request.getHeaders().containsKey("Authorization")) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }

            final String token = request.getHeaders().getOrEmpty("Authorization").get(0).replace("Bearer ", "");

            try {
                if (jwtUtil.validateToken(token)) {
                    Claims claims = jwtUtil.getAllClaimsFromToken(token);
                    String userId = claims.getSubject();
                    String roles = claims.get("roles", String.class);
                    String companyId = claims.get("companyId", String.class);

                    // Add headers to downstream service
                    exchange.getRequest().mutate()
                            .header("X-User-Id", userId)
                            .header("X-User-Roles", roles)
                            .header("X-Company-Id", companyId)
                            .build();
                } else {
                    ServerHttpResponse response = exchange.getResponse();
                    response.setStatusCode(HttpStatus.UNAUTHORIZED);
                    return response.setComplete();
                }

            } catch (Exception e) {
                ServerHttpResponse response = exchange.getResponse();
                response.setStatusCode(HttpStatus.UNAUTHORIZED);
                return response.setComplete();
            }
        }

        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return -1; // Execute before other filters (but after security)
    }
}
