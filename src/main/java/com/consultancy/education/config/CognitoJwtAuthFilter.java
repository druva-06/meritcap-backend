package com.consultancy.education.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class CognitoJwtAuthFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;

    public CognitoJwtAuthFilter(String userPoolId, String region) {
        String jwkUrl = String.format(
                "https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json",
                region, userPoolId
        );
        this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkUrl).build();
        log.info("CognitoJwtAuthFilter initialized with JWK URL: {}", jwkUrl);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {

        // SHORT-CIRCUIT preflight requests - do not attempt JWT validation on OPTIONS
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            log.debug("OPTIONS preflight request - skipping JWT validation for {}", request.getRequestURI());
            chain.doFilter(request, response);
            return;
        }

        String requestId = UUID.randomUUID().toString();
        MDC.put("requestId", requestId);

        try {
            String authHeader = request.getHeader("Authorization");

            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                String token = authHeader.substring(7);

                try {
                    Jwt jwt = jwtDecoder.decode(token);
                    log.debug("JWT successfully decoded for request: {}", request.getRequestURI());

                    // Validate token_use claim
                    String tokenUse = jwt.getClaimAsString("token_use");
                    if (!"access".equals(tokenUse)) {
                        log.warn("Invalid token_use: {} for request {}", tokenUse, request.getRequestURI());
                        throw new RuntimeException("Only access tokens are allowed");
                    }

                    // Extract Cognito groups and convert to ROLE_ authorities
                    List<String> groups = jwt.getClaimAsStringList("cognito:groups");

                    if (groups == null || groups.isEmpty()) {
                        log.warn("No Cognito groups found for user {}", jwt.getClaimAsString("username"));
                        throw new RuntimeException("No group assigned to user");
                    }

                    String username = jwt.getClaimAsString("username");
                    MDC.put("user", username);

                    List<GrantedAuthority> authorities = groups.stream()
                            .map(g -> new SimpleGrantedAuthority("ROLE_" + g.toUpperCase()))
                            .collect(Collectors.toList());

                    log.info("User {} authenticated with roles {}", username, authorities);

                    // Set Spring Security context
                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(username, null, authorities);
                    SecurityContextHolder.getContext().setAuthentication(authentication);

                } catch (Exception ex) {
                    log.error("JWT authentication failed for request {}: {}", request.getRequestURI(), ex.getMessage(), ex);
                    SecurityContextHolder.clearContext();
                    // do NOT short-circuit the chain; let Security handle unauthorized response
                }

            } else {
                log.debug("No Authorization header or not a Bearer token for request: {}", request.getRequestURI());
                MDC.put("user", "anonymous");
            }

            chain.doFilter(request, response);

        } finally {
            log.debug("Request complete: rid={}, status={}", requestId, response.getStatus());
            MDC.clear();
        }
    }
}
