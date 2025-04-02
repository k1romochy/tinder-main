package com.example.demo.stack.Service;

import com.example.demo.stack.Repositrory.Stack;
import com.example.demo.stack.Repositrory.StackRepository;
import com.example.demo.stack.StackMatchingDataUsers.StackMatchingData;
import com.example.demo.stack.StackMatchingDataUsers.StackMatchingDataRepository;
import com.example.demo.user.Repository.User;
import com.example.demo.user.Repository.UserRepository;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    @Autowired
    public StackService(StackRepository stackRepository,
                        UserRepository userRepository,
                        KafkaTemplate<String, Object> kafkaTemplate,
                        StackMatchingDataRepository stackMatchingDataRepository) {
        this.stackRepository = stackRepository;
        this.userRepository = userRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.stackMatchingDataRepository = stackMatchingDataRepository;
    }

    @KafkaListener(topics = "${kafka.topic.preferences}", groupId = "${spring.kafka.consumer.group-id}")
    public void saveMatchingData(User user) {
        StackMatchingData stackMatchingData = new StackMatchingData();
        stackMatchingData.setUser(user);
        stackMatchingDataRepository.save(stackMatchingData);
    }

    public List<User> getAllUsersSuitableToUserPreferences(Long userId, int ageDiff) {
        User user = userRepository.findById(userId).get();
        Point point = user.getPoint();
        int age = user.getPreferences().getAge();
        int minAge = age - ageDiff;
        int maxAge = age + ageDiff;
        return userRepository.findCompatibleNearbyUsers(userId, point, minAge, maxAge);
    }

    @Scheduled(cron = "0 0 3 * * ?")
    public void processingUserStacks() {
        List<StackMatchingData> stackMatchingData = stackMatchingDataRepository.findAll();
        for (StackMatchingData matchingData: stackMatchingData) {
            User user = matchingData.getUser();
            List<User> usersSuitableToUser = getAllUsersSuitableToUserPreferences(user.getId(), 2);


        }
    }
}
