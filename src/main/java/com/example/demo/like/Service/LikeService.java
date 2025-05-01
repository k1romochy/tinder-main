package com.example.demo.like.Service;

import com.example.demo.like.Repository.Like;
import com.example.demo.like.Repository.LikeRepository;
import com.example.demo.notification.Notification;
import com.example.demo.user.Repository.User;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Like> likeRedisTemplate;
    private final SimpMessagingTemplate simpMessagingTemplate;

    @Value("${kafka.topic.match}")
    private String topicName;

    public LikeService(LikeRepository likeRepository, KafkaTemplate<String,
                        Object> kafkaTemplate,
                       RedisTemplate<String, Like> likeRedisTemplate,
                       SimpMessagingTemplate simpMessagingTemplate) {
        this.likeRepository = likeRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.likeRedisTemplate = likeRedisTemplate;
        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    public Map<String, String> handleUserLike(Like like) {
        likeRepository.save(like);
        String redisKey = "like:" + like.getUser().getId() + ":" + like.getUserTargetId();
        likeRedisTemplate.opsForValue().set(redisKey, like, 10, TimeUnit.DAYS);
        User user = like.getUser();

        String reverseRedisKey = "like:" + like.getUserTargetId() + ":" + like.getUser().getId();

        if (isMatch(like)) {
            Map<String, String> responseUserMatch = new HashMap<>();
            responseUserMatch.put("Message", "Match!");
            kafkaTemplate.send(topicName, like);
            Notification notification = new Notification(
                    like.getUser().getId(),
                    like.getUserTargetId(),
                    "You have match with " + like.getUser().getId().toString(),
                    LocalDateTime.now(),
                    "MATCH");
            simpMessagingTemplate.convertAndSendToUser(
                    like.getUserTargetId().toString(),
                    "/queue/notifications",
                    notification
            );
            like.setMatch(true);
            return responseUserMatch;
        }
        else {
            Notification notification = new Notification(
                    like.getUser().getId(),
                    like.getUserTargetId(),
                    "User " + like.getUser().getId().toString() + " liked you!",
                    LocalDateTime.now(),
                    "LIKE");
            simpMessagingTemplate.convertAndSendToUser(
                    like.getUserTargetId().toString(),
                    "/queue/notifications",
                    notification
            );
            Map<String, String> responseUserUnMatch = new HashMap<>();
            responseUserUnMatch.put("Message", "You liked user");
            return responseUserUnMatch;
        }
    }

    public Boolean isMatch(Like like) {
        String reverseRedisKey = "like:" + like.getUserTargetId() + ":" + like.getUser().getId();

        return likeRedisTemplate.hasKey(reverseRedisKey);
    }

    public List<User> getMatchedById(Long id) {
        return likeRepository.findMatchedUsers(id);
    }

    @Transactional
    @KafkaListener(topics = "${kafka.topic.match}", groupId = "${spring.kafka.consumer.group-id}")
    public String handleMatch(Like like) {
        Long userId = like.getUser().getId();
        Long userTargetId = like.getUserTargetId();

        like.setMatch(true);
        likeRepository.save(like);

        Optional<Like> targetLike = likeRepository.findReverseLike(userId, userTargetId);

        if (targetLike.isPresent()) {
            Like tLike = targetLike.get();
            tLike.setMatch(true);
            likeRepository.save(tLike);
        }
        return "Succesfull";
    }
}
