package com.example.demo.preferences.Service;

import com.example.demo.preferences.Repository.Preferences;
import com.example.demo.preferences.Repository.PreferencesRepository;
import com.example.demo.security.SecurityUser;
import com.example.demo.user.Repository.User;
import com.example.demo.user.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class PreferencesService {
    private final PreferencesRepository preferencesRepository;
    private final UserRepository userRepository;

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${kafka.topic.preferences}")
    private String topic;

    @Autowired
    public PreferencesService(PreferencesRepository preferencesRepository,
                              KafkaTemplate<String, Object> kafkaTemplate,
                              UserRepository userRepository) {
        this.preferencesRepository = preferencesRepository;
        this.kafkaTemplate = kafkaTemplate;
        this.userRepository = userRepository;
    }

    public Preferences savePreferences(Preferences preferences,
                                       SecurityUser securityUser) {
        preferencesRepository.save(preferences);
        User user = securityUser.getUser();
        preferences.setUser(user);
        user.setPreferences(preferences);
        userRepository.save(user);

        String key = "preferences_user:" + user.getId().toString();
        kafkaTemplate.send(topic, key, preferences);
        return preferences;
    }
}
