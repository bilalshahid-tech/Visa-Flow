package com.visaflow.user.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            try {
                // In a real implementation, parse the JWT, validate signature, extract claims.
                // For demonstration, we simply populate a dummy authenticated user if token is present.
                // You should replace this with standard JWT parsing logic (e.g., using jjwt).
                
                UUID userId = UUID.randomUUID(); // Extracted from JWT subject
                UUID companyId = UUID.randomUUID(); // Extracted from JWT claims
                
                UserPrincipal principal = new UserPrincipal(userId, companyId, "test@example.com");
                
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        principal, null, Collections.emptyList());
                
                SecurityContextHolder.getContext().setAuthentication(authentication);
            } catch (Exception e) {
                // Ignore invalid token
            }
        }

        filterChain.doFilter(request, response);
    }
}
