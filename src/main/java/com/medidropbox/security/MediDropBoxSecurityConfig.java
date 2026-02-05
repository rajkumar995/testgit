package com.medidropbox.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.Arrays;
import java.util.List;

/**
 * Security configuration for MediDropBox
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
public class MediDropBoxSecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;
    private final SecurityHeadersFilter securityHeadersFilter;
    
    @Value("${spring.web.cors.allowed-origins:*}")
    private String allowedOrigins;
    
    public MediDropBoxSecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                                    MediDropBoxUserDetailsService userDetailsService,
                                    SecurityHeadersFilter securityHeadersFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.userDetailsService = userDetailsService;
        this.securityHeadersFilter = securityHeadersFilter;
    }
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }
    
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/otp/**").permitAll() // OTP generation and verification
                .requestMatchers("/api/v1/patients/register").permitAll() // Patient registration
                .requestMatchers("/api/v1/hospitals").permitAll() // POST for registration
                .requestMatchers("/api/v1/hospitals/public").permitAll() // GET /api/v1/hospitals/public (search with filters)
                .requestMatchers("/api/v1/hospitals/*/public").permitAll() // GET /api/v1/hospitals/{id}/public
                .requestMatchers("/api/v1/doctors/public").permitAll() // GET /api/v1/doctors/public (search with filters)
                .requestMatchers("/api/v1/doctors/*/public").permitAll() // GET /api/v1/doctors/{id}/public
                .requestMatchers("/api/v1/doctors/hospital/*/public").permitAll() // GET /api/v1/doctors/hospital/{id}/public
                .requestMatchers("/api/v1/booking-shares/token/**").permitAll() // GET /api/v1/booking-shares/token/{token}
                .requestMatchers("/api/v1/booking-shares/view/**").permitAll() // GET /api/v1/booking-shares/view/{shortCode}
                .requestMatchers("/shared/**").permitAll() // Thymeleaf shared booking page (no auth)
                .requestMatchers("/swagger-ui/**", "/api-docs/**", "/v3/api-docs/**").permitAll()
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        
        // Allow all origins - Use allowedOriginPatterns for wildcard with credentials
        if ("*".equals(allowedOrigins) || allowedOrigins == null || allowedOrigins.trim().isEmpty()) {
            // Allow all origins using pattern (works with allowCredentials)
            configuration.setAllowedOriginPatterns(List.of("*"));
        } else {
            // Specific origins only (production)
            configuration.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        }
        
        // Allow all HTTP methods
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS", "HEAD"));
        
        // Allow all headers
        configuration.setAllowedHeaders(List.of("*"));
        
        // Allow credentials (cookies, authorization headers)
        configuration.setAllowCredentials(true);
        
        // Cache preflight requests for 1 hour
        configuration.setMaxAge(3600L);
        
        // Expose custom headers to frontend
        configuration.setExposedHeaders(Arrays.asList(
            "Authorization", 
            "Content-Type", 
            "X-Total-Count", 
            "X-Page-Number",
            "X-Page-Size"
        ));
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
