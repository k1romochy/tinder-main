package com.example.demo.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.Optional;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
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
import org.springframework.web.bind.annotation.CookieValue;

import com.example.demo.user.Repository.User;
import com.example.demo.user.Repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RedisTemplate<String, User> userRedisTemplate;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    public AuthController(AuthenticationManager authenticationManager, UserRepository userRepository,
            PasswordEncoder passwordEncoder, JwtService jwtService, RedisTemplate<String, User> userRedisTemplate,
                          KafkaTemplate<String, Object> kafkaTemplate) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.userRedisTemplate = userRedisTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }
    private void syncUserData(User user) {
        String redisKey = "user:" + user.getId();
        if (userRedisTemplate.opsForValue().get(redisKey) == null) {
            userRedisTemplate.opsForValue().set(redisKey, user, 21, TimeUnit.DAYS);
        }
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody AuthRequest request, HttpServletResponse response) {
        try {
            logger.debug("Попытка входа с email: {}", request.getEmail());

            Optional<User> userOptional = userRepository.findByEmailOrName(request.getEmail());
            if (userOptional.isEmpty()) {
                logger.error("Пользователь не найден: {}", request.getEmail());
                return ResponseEntity.status(401).body("Пользователь не найден");
            }
            
            User user = userOptional.get();
            logger.debug("Найден пользователь: id={}, email={}, name={}", 
                user.getId(), user.getEmail(), user.getName());
            
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            
            SecurityContextHolder.getContext().setAuthentication(authentication);
            
            SecurityUser securityUser = (SecurityUser) authentication.getPrincipal();

            logger.debug("Пользователь аутентифицирован: id={}, email={}, name={}", 
                user.getId(), user.getEmail(), user.getName());
            
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
        } catch (Exception e) {
            logger.error("Ошибка аутентификации: {}", e.getMessage(), e);
            return ResponseEntity.status(401).body("Ошибка аутентификации: " + e.getMessage());
        }
    }
    
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest request) {
        try {
            logger.debug("Запрос на регистрацию: email={}, name={}", request.getEmail(), request.getName());
            
            if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                return ResponseEntity.badRequest().body("Error: Email is already taken!");
            }
            
            User user = new User(
                    null,
                    request.getEmail(),
                    request.getName(),
                    passwordEncoder.encode(request.getPassword()));

            user.setActive(true);

            User savedUser = userRepository.save(user);
            logger.debug("Зарегистрирован пользователь: id={}, email={}, name={}", 
                savedUser.getId(), savedUser.getEmail(), savedUser.getName());
            
            String redisKey = "user:" + savedUser.getId();
            userRedisTemplate.opsForValue().set(redisKey, savedUser, 30, TimeUnit.DAYS);
            kafkaTemplate.send("${kafka.topic.preferences}", savedUser);

            return ResponseEntity.ok("User registered successfully!");
        } catch (Exception e) {
            logger.error("Ошибка при регистрации: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body("Error during registration: " + e.getMessage());
        }
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
    
    @PostMapping("/refresh")
    public ResponseEntity<?> refreshToken(@CookieValue(name = "jwt", required = false) String token, 
                                        HttpServletResponse response) {
        if (token == null) {
            return ResponseEntity.status(401).body("No token found");
        }
        
        String refreshedToken = jwtService.refreshToken(token);
        if (refreshedToken != null) {
            Cookie jwtCookie = new Cookie("jwt", refreshedToken);
            jwtCookie.setPath("/");
            jwtCookie.setHttpOnly(true);
            jwtCookie.setMaxAge(24 * 60 * 60 * 7);
            response.addCookie(jwtCookie);
            
            return ResponseEntity.ok().body("Token refreshed successfully");
        } else {
            return ResponseEntity.status(401).body("Unable to refresh token");
        }
    }
}