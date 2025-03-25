package com.example.demo.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.StringUtils;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private static final Logger logger = LoggerFactory.getLogger(JwtAuthenticationFilter.class);
    
    private final JwtService jwtService;
    private final SecurityUserDetailsService userDetailsService;
    
    public JwtAuthenticationFilter(JwtService jwtService, SecurityUserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        String path = request.getServletPath();
        
        if (path.startsWith("/api/auth/login") || path.startsWith("/api/auth/register")) {
            logger.debug("Skipping JWT authentication for authentication endpoint: {}", path);
            filterChain.doFilter(request, response);
            return;
        }
        
        try {
            String jwt = getJwtFromRequest(request);
            
            if (jwt != null) {
                try {
                    String username = jwtService.extractUsername(jwt);
                    
                    if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        
                        if (jwtService.validateToken(jwt, userDetails)) {
                            setAuthentication(request, userDetails);
                            logger.debug("Valid token - user authenticated: {}", username);
                        }
                    }
                } catch (ExpiredJwtException e) {
                    logger.debug("JWT token has expired, trying to refresh...");
                    
                    String username = e.getClaims().getSubject();
                    logger.debug("Expired token for user: {}", username);
                    
                    if (username != null) {
                        try {
                            UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                            
                            String newToken = jwtService.refreshToken(jwt);
                            
                            if (newToken != null) {
                                addTokenCookie(response, newToken);
                                
                                UsernamePasswordAuthenticationToken authentication = 
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                                
                                SecurityContextHolder.getContext().setAuthentication(authentication);
                                
                                logger.debug("JWT token refreshed successfully for user: {}", username);
                            } else {
                                logger.debug("Failed to refresh JWT token, redirecting to login");
                                response.setHeader("X-JWT-Expired", "true");
                            }
                        } catch (Exception userEx) {
                            logger.error("Error refreshing token: {}", userEx.getMessage());
                        }
                    }
                } catch (JwtException e) {
                    logger.debug("Invalid JWT token: {}", e.getMessage());
                    clearJwtCookie(response);
                }
            } else {
                logger.debug("No JWT token found in request: {}", path);
            }
        } catch (Exception e) {
            logger.error("Authentication error: {}", e.getMessage());
        }
        
        filterChain.doFilter(request, response);
    }
    
    private void setAuthentication(HttpServletRequest request, UserDetails userDetails) {
        UsernamePasswordAuthenticationToken authentication = 
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        logger.debug("Authentication set in SecurityContext for user: {}", userDetails.getUsername());
    }
    
    private String getJwtFromRequest(HttpServletRequest request) {
        String jwt = null;
        
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("jwt".equals(cookie.getName()) && StringUtils.hasLength(cookie.getValue())) {
                    jwt = cookie.getValue();
                    logger.debug("JWT found in cookie");
                    break;
                }
            }
        }
        
        if (jwt == null) {
            String bearerToken = request.getHeader("Authorization");
            if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
                jwt = bearerToken.substring(7);
                logger.debug("JWT found in Authorization header");
            }
        }
        
        if (jwt == null) {
            logger.debug("No JWT found in request");
        }
        
        return jwt;
    }
    
    private void addTokenCookie(HttpServletResponse response, String token) {
        Cookie cookie = new Cookie("jwt", token);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(24 * 60 * 60 * 7);
        response.addCookie(cookie);
        logger.debug("Added JWT cookie to response");
    }
    
    private void clearJwtCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setPath("/");
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        logger.debug("Cleared JWT cookie");
    }
}