package com.example.demo.stack.Service;

import com.example.demo.stack.Repositrory.Stack;
import com.example.demo.stack.Repositrory.StackRepository;
import com.example.demo.stack.StackMatchingDataUsers.StackMatchingData;
import com.example.demo.stack.StackMatchingDataUsers.StackMatchingDataRepository;
import com.example.demo.user.Repository.User;
import com.example.demo.user.Repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StackService {
    private final StackRepository stackRepository;
    private final UserRepository userRepository;
    private final StackMatchingDataRepository stackMatchingDataRepository;
    private final GeometryFactory geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Stack> stackRedisTemplate;

    @Autowired
    public StackService(StackRepository stackRepository,
                        UserRepository userRepository,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        StackMatchingDataRepository stackMatchingDataRepository,
                        RedisTemplate<String, Stack> stackRedisTemplate) {
        this.stackRepository = stackRepository;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.stackMatchingDataRepository = stackMatchingDataRepository;
        this.stackRedisTemplate = stackRedisTemplate;
    }

    @Scheduled(cron = "0 0 2 * * ?")
    public void saveActiveUserToMatching() {
        List<User> users = userRepository.findActiveUsers("active");
        for (User user: users) {
            StackMatchingData stackMatchingData = new StackMatchingData();
            stackMatchingData.setUser(user);
            stackMatchingDataRepository.save(stackMatchingData);
        }
    }

    @KafkaListener(topics = "${kafka.topic.preferences}", groupId = "${spring.kafka.consumer.group-id}")
    public void getUserStackInTime(String userJson) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            User user = objectMapper.readValue(userJson, User.class);
            List<User> usersSuitableToUser = getAllUsersSuitableToUserPreferences(user.getId(), 2);
            List<Long> usersSuitableToUserIDs = usersSuitableToUser.stream().map(User::getId)
                    .collect(Collectors.toList());

            Stack stack = new Stack();
            stack.setUsers(usersSuitableToUserIDs);
            stack.setUser(user);

            String key = "UserStack:" + user.getId().toString();

            stackRedisTemplate.opsForValue().set(key, stack);
            stackRepository.save(stack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public List<User> getAllUsersSuitableToUserPreferences(Long userId, int ageDiff) {
        User user = userRepository.findById(userId).get();
        Point point = user.getPoint();
        int age = user.getPreferences().getAge();
        int minAge = age - ageDiff;
        int maxAge = age + ageDiff;
        return userRepository.findCompatibleNearbyUsers(userId, point, minAge, maxAge);
    }

    @Transactional
    @Scheduled(cron = "0 0 3 * * ?")
    public void processingUserStacks() {
        List<StackMatchingData> stackMatchingData = stackMatchingDataRepository.findAll();
        for (StackMatchingData matchingData: stackMatchingData) {
            User user = matchingData.getUser();
            List<User> usersSuitableToUser = getAllUsersSuitableToUserPreferences(user.getId(), 2);
            List<Long> usersSuitableToUserIDs = usersSuitableToUser.stream().map(User::getId)
                    .collect(Collectors.toList());

            Stack stack = new Stack();
            stack.setUsers(usersSuitableToUserIDs);
            stack.setUser(user);

            String key = "UserStack:" + user.getId().toString();

            stackRedisTemplate.opsForValue().set(key, stack);
            stackRepository.save(stack);
            stackMatchingDataRepository.delete(matchingData);
        }
    }
}
