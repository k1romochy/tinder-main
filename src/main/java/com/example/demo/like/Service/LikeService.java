package com.example.demo.like.Service;

import com.example.demo.like.Repository.Like;
import com.example.demo.like.Repository.LikeRepository;
import com.example.demo.user.Repository.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LikeService {
    private final LikeRepository likeRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.match}")
    private String topicName;

    public LikeService(LikeRepository likeRepository, KafkaTemplate<String, Object> kafkaTemplate) {
        this.likeRepository = likeRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Map<String, String> handleUserLike(Like like) {
        likeRepository.save(like);
        User user = like.getUser();
        User userTarget = like.getUserTarget();

        List<Like> userTargetLikes = userTarget.getLikes();
        List<User> userTargetLikesUsers = userTargetLikes.stream().map(Like::getUserTarget).toList();

        if (userTargetLikesUsers.contains(user)) {
            Map<String, String> responseUserMatch = new HashMap<>();
            responseUserMatch.put("Message", "Match!");
            kafkaTemplate.send(topicName, like);
            return responseUserMatch;
        }

        Map<String, String> responseUserUnMatch = new HashMap<>();
        responseUserUnMatch.put("Message", "No match now");
        return responseUserUnMatch;
    }
}
