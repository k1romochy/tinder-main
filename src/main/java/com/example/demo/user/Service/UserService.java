package com.example.demo.user.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.example.demo.user.Repository.User;
import com.example.demo.user.Repository.UserRepository;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);

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
            userRedisTemplate.delete("user"+id.toString());
        } else {
            throw new RuntimeException("User with this id not found");
        }
    }

    public List<User> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users;
    }

    public User saveUserLocation(Long userId, double lat, double lon) {
        User user = findUserById(userId);
        Point point = geometryFactory.createPoint(new Coordinate(lon, lat));
        point.setSRID(4326);
        user.setPoint(point);
        return userRepository.save(user);
    }
}
