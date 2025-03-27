package com.example.demo.security;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import com.example.demo.user.Repository.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service
public class JwtService {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtService.class);
    
    @Value("${jwt.secret:defaultsecretkeydefaultsecretkeydefaultsecretkey}")
    private String secret;
    
    @Value("${jwt.expiration:86400000}")
    private long jwtExpiration;
    
    private Key key;

    @PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        logger.info("JwtService initialized with expiration time: {} ms", jwtExpiration);
    }
    
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
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
    
    private Boolean isTokenExpired(String token) {
        try {
            return extractExpiration(token).before(new Date());
        } catch (ExpiredJwtException e) {
            logger.debug("Token is expired in isTokenExpired check");
            return true;
        }
    }
    
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        try {
            SecurityUser securityUser = (SecurityUser) userDetails;
            User user = securityUser.getUser();
            
            // Используем корректный email для токена, независимо от того, в каком поле он хранится
            String identifier = userDetails.getUsername();
            logger.debug("Generating token for user: {} (id={})", identifier, user.getId());
            
            String token = createToken(claims, identifier);
            logger.debug("Successfully generated token for user: {}", identifier);
            return token;
        } catch (Exception e) {
            logger.error("Error generating token for user {}: {}", userDetails.getUsername(), e.getMessage());
            throw e;
        }
    }
    
    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);
        
        logger.debug("Creating token for user: {} with expiration: {}", subject, expiryDate);
        
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        try {
            final String username = extractUsername(token);
            boolean isValid = username.equals(userDetails.getUsername()) && !isTokenExpired(token);
            logger.debug("Token validation for user {}: {}", username, isValid);
            return isValid;
        } catch (ExpiredJwtException e) {
            logger.debug("JWT token expired during validation");
            return false;
        } catch (JwtException e) {
            logger.debug("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (Exception e) {
            logger.error("JWT validation error: {}", e.getMessage());
            return false;
        }
    }
    
    public String refreshToken(String oldToken) {
        try {
            Claims claims = extractAllClaimsIgnoringExpiration(oldToken);
            
            Date expirationDate = claims.getExpiration();
            Date now = new Date();
            long diffMillis = now.getTime() - expirationDate.getTime();
            
            long maxRefreshPeriodMillis = 7 * 24 * 60 * 60 * 1000;
            if (diffMillis > maxRefreshPeriodMillis) {
                logger.debug("Token expired too long ago ({} ms), refresh not allowed", diffMillis);
                return null;
            }
            
            String username = claims.getSubject();
            
            Map<String, Object> newClaims = new HashMap<>();
            claims.forEach((key, value) -> {
                if (!key.equals("exp") && !key.equals("iat") && !key.equals("nbf") && !key.equals("jti")) {
                    newClaims.put(key, value);
                }
            });
            
            logger.debug("Refreshing token for user: {}", username);
            
            Date newExpiryDate = new Date(System.currentTimeMillis() + jwtExpiration);
            logger.debug("New token expiration: {}", newExpiryDate);
            
            return Jwts.builder()
                    .setClaims(newClaims)
                    .setSubject(username)
                    .setIssuedAt(new Date())
                    .setExpiration(newExpiryDate)
                    .signWith(key, SignatureAlgorithm.HS256)
                    .compact();
        } catch (Exception e) {
            logger.error("Cannot refresh token: {}", e.getMessage());
            return null;
        }
    }
    
    private Claims extractAllClaimsIgnoringExpiration(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .setAllowedClockSkewSeconds(Integer.MAX_VALUE)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
} 