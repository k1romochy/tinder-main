package com.example.demo.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.demo.user.User;
import com.example.demo.user.UserRepository;

import java.util.Optional;

@Service
public class SecurityUserDetailsService implements UserDetailsService {
    
    private static final Logger logger = LoggerFactory.getLogger(SecurityUserDetailsService.class);
    
    private final UserRepository userRepository;
    
    public SecurityUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }
    
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        logger.debug("Attempting to load user with email/name: {}", username);
        
        Optional<User> userOptional = userRepository.findByEmailOrName(username);
        
        if (userOptional.isPresent()) {
            User user = userOptional.get();
            logger.debug("User found: id={}, email={}, name={}", user.getId(), user.getEmail(), user.getName());
            return new SecurityUser(user);
        }
        
        logger.error("User not found with identifier: {}", username);
        throw new UsernameNotFoundException("Пользователь не найден: " + username);
    }
} 