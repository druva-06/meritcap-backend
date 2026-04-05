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
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AdminGetUserRequest;
import software.amazon.awssdk.services.cognitoidentityprovider.model.AttributeType;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UserNotFoundException;

import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
public class CognitoJwtAuthFilter extends OncePerRequestFilter {

    private final JwtDecoder jwtDecoder;
    private final UserRepository userRepository;
    private final CognitoIdentityProviderClient cognitoClient;
    private final String userPoolId;

    public CognitoJwtAuthFilter(String userPoolId, String region, UserRepository userRepository,
            CognitoIdentityProviderClient cognitoClient) {
        String jwkUrl = String.format(
                "https://cognito-idp.%s.amazonaws.com/%s/.well-known/jwks.json",
                region, userPoolId);
        this.jwtDecoder = NimbusJwtDecoder.withJwkSetUri(jwkUrl).build();
        this.userRepository = userRepository;
        this.cognitoClient = cognitoClient;
        this.userPoolId = userPoolId;
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

                        // Accept Cognito access tokens and ID tokens.
                        String tokenUse = jwt.getClaimAsString("token_use");
                        if (tokenUse != null && !"access".equals(tokenUse) && !"id".equals(tokenUse)) {
                            log.warn("Invalid token_use: {} for request {}", tokenUse, request.getRequestURI());
                            throw new RuntimeException("Unsupported token type");
                        }

                        String email = jwt.getClaimAsString("email");
                        String username = jwt.getClaimAsString("username");
                        String cognitoUsername = jwt.getClaimAsString("cognito:username");
                        String preferredUsername = jwt.getClaimAsString("preferred_username");
                        String principal = firstNonBlank(email, preferredUsername, cognitoUsername, username,
                                jwt.getSubject());
                        User localUser = resolveLocalUser(email, username, cognitoUsername, preferredUsername,
                                principal);
                        String effectivePrincipal = localUser != null && localUser.getEmail() != null
                                && !localUser.getEmail().isBlank()
                                        ? localUser.getEmail()
                                        : principal;

                        // Extract Cognito groups and convert to ROLE_ authorities
                        List<String> groups = jwt.getClaimAsStringList("cognito:groups");

                        List<GrantedAuthority> authorities;
                        if (groups == null || groups.isEmpty()) {
                            // Fallback to DB role to avoid accidental ROLE_USER downgrade.
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

                        MDC.put("user", effectivePrincipal != null ? effectivePrincipal : "unknown");

                        log.info("User {} authenticated with roles {}", effectivePrincipal, authorities);

                        // Set Spring Security context
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                effectivePrincipal, null, authorities);
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

    private User resolveLocalUser(String email, String username, String cognitoUsername, String preferredUsername,
            String principal) {
        User localUser = null;

        if (email != null && !email.isBlank()) {
            localUser = userRepository.findByEmailIgnoreCase(email);
        }

        if (localUser == null && preferredUsername != null && !preferredUsername.isBlank()) {
            localUser = userRepository.findByUsernameIgnoreCase(preferredUsername);
            if (localUser == null && preferredUsername.contains("@")) {
                localUser = userRepository.findByEmailIgnoreCase(preferredUsername);
            }
        }

        if (localUser == null && username != null && !username.isBlank()) {
            localUser = userRepository.findByUsernameIgnoreCase(username);
            if (localUser == null && username.contains("@")) {
                localUser = userRepository.findByEmailIgnoreCase(username);
            }
        }

        if (localUser == null && cognitoUsername != null && !cognitoUsername.isBlank()) {
            localUser = userRepository.findByUsernameIgnoreCase(cognitoUsername);
            if (localUser == null && cognitoUsername.contains("@")) {
                localUser = userRepository.findByEmailIgnoreCase(cognitoUsername);
            }
        }

        if (localUser == null && principal != null && !principal.isBlank()) {
            localUser = userRepository.findByUsernameIgnoreCase(principal);
            if (localUser == null && principal.contains("@")) {
                localUser = userRepository.findByEmailIgnoreCase(principal);
            }
        }

        if (localUser == null) {
            localUser = resolveUserByCognitoUsername(cognitoUsername);
        }

        if (localUser == null) {
            localUser = resolveUserByCognitoUsername(username);
        }

        if (localUser == null) {
            localUser = resolveUserByCognitoUsername(principal);
        }

        return localUser;
    }

    private User resolveUserByCognitoUsername(String cognitoUsername) {
        if (cognitoUsername == null || cognitoUsername.isBlank()) {
            return null;
        }
        try {
            var response = cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(cognitoUsername)
                    .build());
            String email = response.userAttributes().stream()
                    .filter(attr -> "email".equalsIgnoreCase(attr.name()))
                    .map(AttributeType::value)
                    .findFirst()
                    .orElse(null);
            if (email != null && !email.isBlank()) {
                return userRepository.findByEmailIgnoreCase(email);
            }
        } catch (UserNotFoundException ignored) {
            log.debug("Cognito username {} not found while resolving local user", cognitoUsername);
        } catch (Exception ex) {
            log.debug("Failed to resolve local user from Cognito username {}: {}", cognitoUsername, ex.getMessage());
        }
        return null;
    }
}
