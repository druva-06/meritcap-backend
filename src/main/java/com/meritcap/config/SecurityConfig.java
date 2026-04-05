package com.meritcap.config;

import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;
import com.meritcap.repository.UserRepository;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);

    @Autowired
    private Environment environment;

    @Autowired
    private UserRepository userRepository;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/signup",
            "/auth/signup/student",
            "/auth/signup/invited",
            "/invitation/validate",
            "/auth/login",
            "/auth/email-otp/**",
            "/auth/google/**",
            "/auth/forgotPassword/**",
            "/auth/resendVerificationCode/**",
            "/auth/confirmVerificationCode",
            "/auth/confirmForgotPassword",
            "/auth/refresh",
            "/actuator/health",
            "/swagger-ui/**",
            "/v3/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        CognitoJwtAuthFilter jwtFilter = buildCognitoJwtAuthFilterIfConfigured();

        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults()) // <-- enable Spring Security to use CorsConfigurationSource bean
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // allow preflight OPTIONS requests without authentication
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint()) // 401 handler
                        .accessDeniedHandler(customAccessDeniedHandler()) // 403 handler
                );

        if (jwtFilter != null) {
            http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
        }

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            String json = String.format(
                    "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required or token is invalid\",\"path\":\"%s\"}",
                    request.getRequestURI());
            response.getWriter().write(json);
        };
    }

    @Bean
    public AccessDeniedHandler customAccessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            String json = String.format(
                    "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to access this resource\",\"path\":\"%s\"}",
                    request.getRequestURI());
            response.getWriter().write(json);
        };
    }

    private CognitoJwtAuthFilter buildCognitoJwtAuthFilterIfConfigured() {
        String userPoolId = firstPresent("aws.cognito.userPoolId", "COGNITO_USER_POOL_ID");
        String region = firstPresent("aws.region", "AWS_REGION");

        if (StringUtils.hasText(userPoolId) && StringUtils.hasText(region)) {
            return new CognitoJwtAuthFilter(userPoolId, region, userRepository);
        }

        if (isDevProfile()) {
            log.warn(
                    "Cognito config missing (aws.cognito.userPoolId/aws.region). JWT auth filter disabled for dev startup.");
            return null;
        }

        throw new IllegalStateException(
                "Missing Cognito configuration. Set aws.cognito.userPoolId (or COGNITO_USER_POOL_ID) and aws.region (or AWS_REGION).");
    }

    private boolean isDevProfile() {
        return Arrays.stream(environment.getActiveProfiles())
                .anyMatch("dev"::equalsIgnoreCase);
    }

    /**
     * CORS configuration bean used by both Spring MVC and Spring Security (via
     * .cors()).
     * Adjust allowedOrigins/allowedHeaders/allowCredentials to your needs.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        String allowedOriginsStr = firstPresent("cors.allowed-origins", "CORS_ALLOWED_ORIGINS");
        if (!StringUtils.hasText(allowedOriginsStr)) {
            allowedOriginsStr = "http://localhost:3000";
        }

        CorsConfiguration configuration = new CorsConfiguration();
        // DEV: set to your dev origin. If you need cookies/auth, use explicit origin
        // and allowCredentials(true).
        configuration.setAllowedOrigins(Arrays.asList(allowedOriginsStr.split(",")));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Explicitly allow Authorization so browser preflight permits the header
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"));
        // Expose Authorization header if the client needs to read it
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true); // if you will send cookies/credentials. Remove if not needed.
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private String firstPresent(String... keys) {
        for (String key : keys) {
            String value = safeGetProperty(key);
            if (StringUtils.hasText(value) && !value.contains("${")) {
                return value.trim();
            }
        }
        return "";
    }

    private String safeGetProperty(String key) {
        try {
            return environment.getProperty(key);
        } catch (IllegalArgumentException ex) {
            return null;
        }
    }
}
