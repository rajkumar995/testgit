package com.medidropbox.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Security Headers Filter
 * Adds security headers to all responses for compliance
 */
@Component
@Order(1)
public class SecurityHeadersFilter extends OncePerRequestFilter {
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                    HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        // HTTPS Enforcement (HSTS)
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        
        // Prevent clickjacking
        response.setHeader("X-Frame-Options", "DENY");
        
        // XSS Protection
        response.setHeader("X-XSS-Protection", "1; mode=block");
        
        // Prevent MIME type sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        
        // Content Security Policy
        response.setHeader("Content-Security-Policy", 
            "default-src 'self'; script-src 'self' 'unsafe-inline'; style-src 'self' 'unsafe-inline'; img-src 'self' data: https:; font-src 'self' data:;");
        
        // Referrer Policy
        response.setHeader("Referrer-Policy", "strict-origin-when-cross-origin");
        
        // Permissions Policy
        response.setHeader("Permissions-Policy", 
            "geolocation=(), microphone=(), camera=()");
        
        // Remove server information
        response.setHeader("X-Powered-By", "");
        
        filterChain.doFilter(request, response);
    }
}
