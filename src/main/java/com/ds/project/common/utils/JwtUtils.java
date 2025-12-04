package com.ds.project.common.utils;

import com.ds.project.common.entities.common.UserPayload;
import com.ds.project.common.interfaces.ISettingService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * JWT utility class for token generation and validation
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtils {
    
    private final ISettingService settingService;
    
    // Default secret key - should be moved to application properties in production
    private static final String DEFAULT_SECRET = "mySecretKey123456789012345678901234567890123456789012345678901234567890";
    
    /**
     * Generate JWT token for user
     */
    public String generateToken(UserPayload userPayload, boolean rememberMe) {
        try {
            long expirationTime = getExpirationTime(rememberMe); // seconds
            long currentTime = Instant.now().getEpochSecond();

            long expEpoch = currentTime + expirationTime;

            userPayload.setIat(currentTime);
            userPayload.setExp(expEpoch);

            Map<String, Object> claims = new HashMap<>();
            claims.put("userId", userPayload.getUserId());
            claims.put("username", userPayload.getUsername());
            claims.put("email", userPayload.getEmail());
            claims.put("firstName", userPayload.getFirstName());
            claims.put("lastName", userPayload.getLastName());
            claims.put("birthday", userPayload.getBirthday());
            claims.put("gender", userPayload.getGender());
            claims.put("phoneNumber", userPayload.getPhoneNumber());
            claims.put("status", userPayload.getStatus());
            claims.put("roles", userPayload.getRoles());
            claims.put("iat", userPayload.getIat());
            claims.put("exp", userPayload.getExp());

            // 🔥 IMPORTANT: Set JWT `iat` & `exp` as standard fields
            return Jwts.builder()
                    .claims(claims)
                    .issuedAt(Date.from(Instant.ofEpochSecond(currentTime)))
                    .expiration(Date.from(Instant.ofEpochSecond(expEpoch)))
                    .signWith(getSigningKey())
                    .compact();

        } catch (Exception e) {
            log.error("Error generating JWT token: {}", e.getMessage());
            throw new RuntimeException("Failed to generate JWT token", e);
        }
    }


    /**
     * Validate JWT token
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }
    
    /**
     * Extract claims from JWT token
     */
    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            log.error("Error extracting claims from token: {}", e.getMessage());
            throw new RuntimeException("Invalid token", e);
        }
    }
    
    /**
     * Extract UserPayload from JWT token
     */
    public UserPayload getUserPayloadFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        
        @SuppressWarnings("unchecked")
        List<String> roles = (List<String>) claims.get("roles");
        
        return UserPayload.builder()
                .userId(claims.get("userId", String.class))
                .username(claims.get("username", String.class))
                .email(claims.get("email", String.class))
                .firstName(claims.get("firstName", String.class))
                .lastName(claims.get("lastName", String.class))
                .birthday(claims.get("birthday", String.class))
                .gender(claims.get("gender", String.class))
                .status(claims.get("status", String.class))
                .phoneNumber(claims.get("phoneNumber", String.class))
                .roles(roles)
                .iat(claims.get("iat", Long.class))
                .exp(claims.get("exp", Long.class))
                .build();
    }
    
    /**
     * Check if token is expired
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = getClaimsFromToken(token);
            Date expiration = claims.getExpiration();
            return expiration.before(new Date());
        } catch (Exception e) {
            log.error("Error checking token expiration: {}", e.getMessage());
            return true;
        }
    }
    
    /**
     * Get expiration time based on rememberMe flag and settings
     */
    public long getExpirationTime(boolean rememberMe) {
        try {
            if (rememberMe) {
                // Try to get save login duration from settings
                return settingService.getSettingByKey("JWT_SAVE_LOGIN_DURATION")
                        .map(setting -> Long.parseLong(setting.getResult().orElseThrow().getValue()))
                        .orElse(86400L); // Default: 1 day
            } else {
                // Try to get expire time from settings
                return settingService.getSettingByKey("JWT_EXPIRE_TIME")
                        .map(setting -> Long.parseLong(setting.getResult().orElseThrow().getValue()))
                        .orElse(3600L); // Default: 1 hour
            }
        } catch (Exception e) {
            log.warn("Error getting expiration time from settings, using defaults: {}", e.getMessage());
            return rememberMe ? 86400L : 3600L; // Fallback to defaults
        }
    }
    
    /**
     * Get signing key
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = DEFAULT_SECRET.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
