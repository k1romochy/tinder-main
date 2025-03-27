package com.example.demo.preferences.Service;

import com.example.demo.preferences.Repository.Preferences;
import com.example.demo.preferences.Repository.PreferencesRepository;
import com.example.demo.security.SecurityUser;
import com.example.demo.user.Repository.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PreferencesService {
    private final PreferencesRepository preferencesRepository;

    private final KafkaTemplate<String, Preferences> kafkaTemplate;

    @Value("${kafka.topic.example}")
    private String topic;

    @Autowired
    public PreferencesService(PreferencesRepository preferencesRepository, KafkaTemplate kafkaTemplate) {
        this.preferencesRepository = preferencesRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    public Preferences savePreferences(Preferences preferences,
                                       SecurityUser securityUser) {
        User user = securityUser.getUser();
        preferences.setUser(user);
        preferencesRepository.save(preferences);

        String key = "preferences_user:" + user.getId().toString();
        kafkaTemplate.send(topic, key, preferences);
        return preferences;
    }
}
