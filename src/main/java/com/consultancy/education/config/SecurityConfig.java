package com.consultancy.education.config;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class SecurityConfig {
    @Value("${aws.cognito.userPoolId}")
    private String userPoolId;

    @Value("${aws.region}")
    private String region;

    private static final String[] PUBLIC_ENDPOINTS = {
            "/auth/signup",
            "/auth/login",
            "/auth/forgotPassword/**",
            "/auth/resendVerificationCode/**",
            "/auth/confirmVerificationCode",
            "/auth/confirmForgotPassword",
            "/swagger-ui/**",
            "/v3/**"
    };

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults()) // <-- enable Spring Security to use CorsConfigurationSource bean
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // allow preflight OPTIONS requests without authentication
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(customAuthenticationEntryPoint()) // 401 handler
                        .accessDeniedHandler(customAccessDeniedHandler()) // 403 handler
                )
                .addFilterBefore(cognitoJwtAuthFilter(), UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public AuthenticationEntryPoint customAuthenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            String json = String.format(
                    "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required or token is invalid\",\"path\":\"%s\"}",
                    request.getRequestURI()
            );
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
                    request.getRequestURI()
            );
            response.getWriter().write(json);
        };
    }

    @Bean
    public CognitoJwtAuthFilter cognitoJwtAuthFilter() {
        return new CognitoJwtAuthFilter(userPoolId, region);
    }

    /**
     * CORS configuration bean used by both Spring MVC and Spring Security (via .cors()).
     * Adjust allowedOrigins/allowedHeaders/allowCredentials to your needs.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        // DEV: set to your dev origin. If you need cookies/auth, use explicit origin and allowCredentials(true).
        configuration.setAllowedOrigins(List.of("http://localhost:3000", "http://127.0.0.1:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        // Explicitly allow Authorization so browser preflight permits the header
        configuration.setAllowedHeaders(List.of(
                "Authorization",
                "Content-Type",
                "Accept",
                "Origin",
                "X-Requested-With",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));
        // Expose Authorization header if the client needs to read it
        configuration.setExposedHeaders(List.of("Authorization"));
        configuration.setAllowCredentials(true); // if you will send cookies/credentials. Remove if not needed.
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}