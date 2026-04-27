package com.visaflow.gateway.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
public class FallbackController {

    @RequestMapping("/fallback/auth")
    public Mono<ResponseEntity<Map<String, String>>> authFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "Auth service unavailable")));
    }

    @RequestMapping("/fallback/user")
    public Mono<ResponseEntity<Map<String, String>>> userFallback() {
        return Mono.just(ResponseEntity
                .status(HttpStatus.SERVICE_UNAVAILABLE)
                .body(Map.of("error", "User service unavailable")));
    }
}
