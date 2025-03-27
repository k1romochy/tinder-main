package com.example.demo.user.Controller;

import java.util.List;
import java.util.Set;

import com.example.demo.user.Repository.User;
import com.example.demo.user.Service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users/")
public class UserController {
    private final UserService userService;

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("{id}/set_role/")
    public Set<String> setUserRoleById(@PathVariable Long id, String role) {
        return userService.setUserRoleById(id, role);
    }

    @GetMapping("{id}/")
    public User geUserById(@PathVariable Long id) {
        return userService.findUserById(id);
    }

    @GetMapping("")
    public List<User> getUsers() {
        return userService.getAllUsers();
    }

}
