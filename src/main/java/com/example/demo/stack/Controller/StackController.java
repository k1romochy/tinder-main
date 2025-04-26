package com.example.demo.stack.Controller;

import com.example.demo.like.Repository.Like;
import com.example.demo.like.Repository.LikeDTO;
import com.example.demo.like.Service.LikeService;
import com.example.demo.security.SecurityUser;
import com.example.demo.stack.Service.StackService;
import com.example.demo.user.Repository.User;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/stack/")
public class StackController {
    private final StackService stackService;
    private final LikeService likeService;

    public StackController(StackService stackService, LikeService likeService) {
        this.stackService = stackService;
        this.likeService = likeService;
    }

    @GetMapping()
    public List<Long> getSuitUser(@AuthenticationPrincipal SecurityUser securityUser) {
        Long id = securityUser.getUser().getId();
        return stackService.getSuitableUsersById(id);
    }

    @PostMapping("like/")
    public Map<String, String> handleLike(@Valid @RequestBody LikeDTO likeDTO,
                                          @AuthenticationPrincipal SecurityUser securityUser) {
        Long userTargetId = likeDTO.getUserTargetId();
        User user = securityUser.getUser();
        Like like = new Like();
        like.setUser(user);
        like.setUserTarget(userTargetId);

        return likeService.handleUserLike(like);
    }

    @GetMapping("matches")
    public List<User> getMatches(@AuthenticationPrincipal SecurityUser securityUser) {
        Long id = securityUser.getUser().getId();

        return likeService.getMatchedById(id);
    }
}
