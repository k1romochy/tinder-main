package com.example.demo.preferences.Service;

import com.example.demo.preferences.Repository.Preferences;
import com.example.demo.preferences.Repository.PreferencesRepository;
import com.example.demo.security.SecurityUser;
import com.example.demo.user.Repository.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class PreferencesService {
    private final PreferencesRepository preferencesRepository;

    @Autowired
    public PreferencesService(PreferencesRepository preferencesRepository) {
        this.preferencesRepository = preferencesRepository;
    }

    public Preferences savePreferences(Preferences preferences,
                                       SecurityUser securityUser) {
        User user = securityUser.getUser();
        preferences.setUser(user);
        preferencesRepository.save(preferences);
        return preferences;
    }
}
