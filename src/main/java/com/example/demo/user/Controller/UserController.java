package com.example.demo.user.Controller;

import java.util.List;
import java.util.Set;

import com.example.demo.security.SecurityUser;
import com.example.demo.user.Repository.User;
import com.example.demo.user.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users/")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("{id}/")
    public User geUserById(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    @GetMapping("")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

    @PostMapping("save-location/")
    public User setUserLocation(@AuthenticationPrincipal SecurityUser securityUser,
                                @RequestBody double lat, @RequestBody double lon) {
        Long userId = securityUser.getUser().getId();
        return userService.saveUserLocation(userId, lat, lon);
    }
}
