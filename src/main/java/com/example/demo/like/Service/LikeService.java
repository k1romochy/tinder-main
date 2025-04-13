package com.example.demo.like.Service;

import com.example.demo.like.Repository.Like;
import com.example.demo.like.Repository.LikeRepository;
import com.example.demo.user.Repository.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final RedisTemplate<String, Like> likeRedisTemplate;

    @Value("${kafka.topic.match}")
    private String topicName;

    public LikeService(LikeRepository likeRepository, KafkaTemplate<String,
                        Object> kafkaTemplate,
                       RedisTemplate<String, Like> likeRedisTemplate) {
        this.likeRepository = likeRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.likeRedisTemplate = likeRedisTemplate;
    }

    public Map<String, String> handleUserLike(Like like) {
        likeRepository.save(like);
        String redisKey = "like:" + like.getUser().getId() + ":" + like.getUserTarget().getId();
        likeRedisTemplate.opsForValue().set(redisKey, like, 10, TimeUnit.DAYS);
        User user = like.getUser();
        User userTarget = like.getUserTarget();

        String reverseRedisKey = "like:" + like.getUserTarget().getId() + ":" + like.getUser().getId();

        if (likeRedisTemplate.hasKey(reverseRedisKey)) {
            Map<String, String> responseUserMatch = new HashMap<>();
            responseUserMatch.put("Message", "Match!");
            kafkaTemplate.send(topicName, like);
            return responseUserMatch;
        }
        else {
            Map<String, String> responseUserUnMatch = new HashMap<>();
            responseUserUnMatch.put("Message", "No match now");
            return responseUserUnMatch;
        }
    }
}
