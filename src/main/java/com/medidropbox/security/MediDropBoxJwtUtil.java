package com.medidropbox.security;

import com.medidropbox.enums.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * JWT utility for MediDropBox
 */
@Component
public class MediDropBoxJwtUtil {
    
    private static final String SECRET_KEY = "MediDropBox-Secret-Key-2024";
    public static final long ACCESS_TOKEN_VALIDITY = 1000 * 60 * 60 * 6; // 6 hours
    public static final long REFRESH_TOKEN_VALIDITY = 1000 * 60 * 60 * 24 * 7; // 7 days
    
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }
    
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }
    
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }
    
    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
    
    public boolean isTokenExpiredPublic(String token) {
        try {
            return isTokenExpired(token);
        } catch (Exception e) {
            return true;
        }
    }
    
    public String generateAccessToken(UserDetails userDetails, Role role, List<String> permissions) {
        return generateAccessToken(userDetails, role, permissions, null);
    }
    
    public String generateAccessToken(UserDetails userDetails, Role role, List<String> permissions, Long hospitalId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role.name());
        if (permissions != null && !permissions.isEmpty()) {
            claims.put("permissions", permissions);
        }
        // Add hospitalId to JWT claims if provided (for HOSPITAL_ADMIN, HOSPITAL_STAFF, DOCTOR roles)
        if (hospitalId != null) {
            claims.put("hospitalId", hospitalId);
        }
        return createToken(claims, userDetails.getUsername(), ACCESS_TOKEN_VALIDITY);
    }
    
    public String generateRefreshToken(String username) {
        return createToken(new HashMap<>(), username, REFRESH_TOKEN_VALIDITY);
    }
    
    private String createToken(Map<String, Object> claims, String subject, long validityMs) {
        Date now = new Date();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + validityMs))
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
    }
    
    public Role extractRole(String token) {
        try {
            String roleStr = extractClaim(token, claims -> claims.get("role", String.class));
            return roleStr != null ? Role.valueOf(roleStr) : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    @SuppressWarnings("unchecked")
    public List<String> extractPermissions(String token) {
        try {
            return extractClaim(token, claims -> (List<String>) claims.get("permissions"));
        } catch (Exception e) {
            return null;
        }
    }
}
