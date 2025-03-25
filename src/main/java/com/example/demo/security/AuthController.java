package com.example.demo.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, User> userRedisTemplate;
    
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, RedisTemplate<String, User> userRedisTemplate) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userRedisTemplate = userRedisTemplate;
    }
    private void syncUserData(User user) {
        String redisKey = "user:" + user.getId();
        if (userRedisTemplate.opsForValue().get(redisKey) == null) {
            userRedisTemplate.opsForValue().set(redisKey, user, 21, TimeUnit.DAYS);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request, HttpServletResponse response) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
        
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();
        User user = securityUser.getUser();
        
        String jwt = jwtService.generateToken(securityUser);
        
        Cookie jwtCookie = new Cookie("jwt", jwt);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(24 * 60 * 60 * 7);
        response.addCookie(jwtCookie);
        
        syncUserData(user);
        
        return ResponseEntity.ok(new AuthResponse(
                jwt,
                user.getId(),
                user.getName(),
                user.getEmail()));
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return ResponseEntity.badRequest().body("Error: Email is already taken!");
        }
        
        User user = new User(
                null,
                request.getName(),
                request.getEmail(),
                passwordEncoder.encode(request.getPassword()));
        
        User savedUser = userRepository.save(user);
        
        String redisKey = "user:" + savedUser.getId();
        userRedisTemplate.opsForValue().set(redisKey, savedUser, 30, TimeUnit.DAYS);

        return ResponseEntity.ok("User registered successfully!");
    }
    
    @PostMapping("/logout")
    public Map<String, Object> logout(HttpServletResponse response) {
        Cookie jwtCookie = new Cookie("jwt", null);
        jwtCookie.setPath("/");
        jwtCookie.setHttpOnly(true);
        jwtCookie.setMaxAge(0);
        response.addCookie(jwtCookie);
        
        Map<String, Object> responseUser = new HashMap<>();
        responseUser.put("Message", "Logged out succeful");
        return responseUser;
    }
    
    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@AuthenticationPrincipal SecurityUser securityUser) {
        if (securityUser == null) {
            return ResponseEntity.status(401).body("Not authenticated");
        }
        
        User user = securityUser.getUser();
        return ResponseEntity.ok(new AuthResponse(
                null,
                user.getId(),
                user.getName(),
                user.getEmail()));
    }
}