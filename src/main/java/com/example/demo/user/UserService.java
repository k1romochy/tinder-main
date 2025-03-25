package com.example.demo.user;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;

    private final RedisTemplate<String, User> userRedisTemplate;

    @Autowired
    public UserService(UserRepository userRepository, RedisTemplate<String, User> userRedisTemplate) {
        this.userRedisTemplate = userRedisTemplate;
        this.userRepository = userRepository;
    }

    @Cacheable(value="getUser", key="#id")
    public User findUserById (Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User with this id not found"));
    }

    public void deleteUserById(Long id) {
        if(userRepository.findById(id).isPresent()) {
            userRepository.deleteById(id);
        } else {
            throw new RuntimeException("User with this id not found");
        }
    }

    public Set<String> setUserRoleById(Long id, String role) {
        User user = findUserById(id);
        user.addRole(role);
        return user.getRoles();
    }

    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users;
    }
}
