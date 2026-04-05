package com.meritcap.config;

import com.meritcap.model.User;
import com.meritcap.repository.UserRepository;
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
    private final UserRepository userRepository;

    public CognitoJwtAuthFilter(String userPoolId, String region, UserRepository userRepository) {
        String jwkUrl = String.format(
                "https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json",
                region, userPoolId);
        this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkUrl).build();
        this.userRepository = userRepository;
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

                // Handle OTP session tokens differently from Cognito JWT tokens
                if (token.startsWith("otp-session-")) {
                    log.debug("OTP session token detected for request: {}", request.getRequestURI());
                    
                    try {
                        // Extract user ID from OTP session token: "otp-session-{userId}-{timestamp}"
                        String[] parts = token.split("-");
                        if (parts.length >= 3) {
                            String userId = parts[2];
                            
                            // Set simple authentication for OTP users
                            List<GrantedAuthority> authorities = List.of(
                                new SimpleGrantedAuthority("ROLE_STUDENT"),
                                new SimpleGrantedAuthority("ROLE_USER")
                            );
                            
                            String username = "otp-user-" + userId;
                            MDC.put("user", username);
                            
                            log.info("OTP session user {} authenticated with roles {}", username, authorities);
                            
                            // Set Spring Security context
                            UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                    username, null, authorities);
                            SecurityContextHolder.getContext().setAuthentication(authentication);
                        } else {
                            log.warn("Invalid OTP session token format: {}", token);
                        }
                    } catch (Exception ex) {
                        log.error("OTP session authentication failed for request {}: {}", request.getRequestURI(), ex.getMessage());
                        SecurityContextHolder.clearContext();
                    }
                } else {
                    // Handle regular Cognito JWT tokens
                    try {
                        Jwt jwt = jwtDecoder.decode(token);
                        log.debug("JWT successfully decoded for request: {}", request.getRequestURI());

                        // Validate token_use claim
                        String tokenUse = jwt.getClaimAsString("token_use");
                        if (!"access".equals(tokenUse)) {
                            log.warn("Invalid token_use: {} for request {}", tokenUse, request.getRequestURI());
                            throw new RuntimeException("Only access tokens are allowed");
                        }

                        String email = jwt.getClaimAsString("email");
                        String username = jwt.getClaimAsString("username");
                        String cognitoUsername = jwt.getClaimAsString("cognito:username");
                        String principal = firstNonBlank(email, cognitoUsername, username);

                        // Extract Cognito groups and convert to ROLE_ authorities
                        List<String> groups = jwt.getClaimAsStringList("cognito:groups");

                        List<GrantedAuthority> authorities;
                        if (groups == null || groups.isEmpty()) {
                            // Fallback to DB role to avoid accidental ROLE_USER downgrade.
                            User localUser = null;
                            if (email != null && !email.isBlank()) {
                                localUser = userRepository.findByEmailIgnoreCase(email);
                            }
                            if (localUser == null && username != null && username.contains("@")) {
                                localUser = userRepository.findByEmailIgnoreCase(username);
                            }
                            if (localUser == null && username != null && !username.isBlank()) {
                                localUser = userRepository.findByUsername(username);
                            }
                            if (localUser == null && cognitoUsername != null && cognitoUsername.contains("@")) {
                                localUser = userRepository.findByEmailIgnoreCase(cognitoUsername);
                            }
                            if (localUser == null && cognitoUsername != null && !cognitoUsername.isBlank()) {
                                localUser = userRepository.findByUsername(cognitoUsername);
                            }

                            if (localUser != null && localUser.getRole() != null && localUser.getRole().getName() != null) {
                                String roleName = localUser.getRole().getName().toUpperCase();
                                authorities = List.of(new SimpleGrantedAuthority("ROLE_" + roleName));
                                log.info("No Cognito groups found; mapped DB role {} for user {}", roleName, localUser.getEmail());
                            } else {
                                log.warn("No Cognito groups and no DB role match for user {}, assigning ROLE_USER",
                                        principal);
                                authorities = List.of(new SimpleGrantedAuthority("ROLE_USER"));
                            }
                        } else {
                            authorities = groups.stream()
                                    .map(g -> new SimpleGrantedAuthority("ROLE_" + g.toUpperCase()))
                                    .collect(Collectors.toList());
                        }

                        MDC.put("user", principal != null ? principal : "unknown");

                        log.info("User {} authenticated with roles {}", principal, authorities);

                        // Set Spring Security context
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                principal, null, authorities);
                        SecurityContextHolder.getContext().setAuthentication(authentication);

                    } catch (Exception ex) {
                        log.error("JWT authentication failed for request {}: {}", request.getRequestURI(), ex.getMessage(),
                                ex);
                        SecurityContextHolder.clearContext();
                        // do NOT short-circuit the chain; let Security handle unauthorized response
                    }
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

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value;
            }
        }
        return null;
    }
}
