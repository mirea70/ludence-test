package com.test.ludens.user.controller;

import com.test.ludens.auth.security.dto.AuthenticatedUser;
import com.test.ludens.common.page.PageRequest;
import com.test.ludens.post.dto.response.PostPageResponse;
import com.test.ludens.user.dto.response.UserResponse;
import com.test.ludens.user.service.UserHeartQueryService;
import com.test.ludens.user.service.UserPostQueryService;
import com.test.ludens.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private final UserPostQueryService userPostQueryService;
    private final UserHeartQueryService userHeartQueryService;

    @GetMapping("/{username}")
    public ResponseEntity<UserResponse> getUser(@PathVariable String username) {
        return ResponseEntity.ok(userService.getUser(username));
    }

    @GetMapping("/{username}/posts")
    public ResponseEntity<PostPageResponse> getUserPosts(
            @PathVariable String username,
            @RequestParam(defaultValue = "" + PageRequest.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + PageRequest.DEFAULT_LIMIT) int limit,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long currentUserId = user == null ? null : user.id();
        return ResponseEntity.ok(userPostQueryService.getUserPosts(username, page, limit, currentUserId));
    }

    @GetMapping("/{username}/hearts")
    public ResponseEntity<PostPageResponse> getUserHearts(
            @PathVariable String username,
            @RequestParam(defaultValue = "" + PageRequest.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + PageRequest.DEFAULT_LIMIT) int limit,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        return ResponseEntity.ok(userHeartQueryService.getUserHearts(user.id(), username, page, limit));
    }
}
