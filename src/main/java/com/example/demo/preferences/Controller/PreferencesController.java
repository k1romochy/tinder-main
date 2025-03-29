package com.example.demo.preferences.Controller;

import com.example.demo.preferences.Repository.Preferences;
import com.example.demo.preferences.Service.PreferencesService;
import com.example.demo.security.SecurityUser;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user-preferences/")
public class PreferencesController {
    private final PreferencesService preferencesService;

    public PreferencesController(PreferencesService preferencesService) {
        this.preferencesService = preferencesService;
    }

    @PostMapping("/save/")
    public Preferences setPreferences(@RequestBody Preferences preferences,
                                      @AuthenticationPrincipal SecurityUser securityUser) {
        preferencesService.savePreferences(preferences, securityUser);
        return preferences;
    }
}
