package com.test.ludence.user.controller;

import com.test.ludence.auth.security.AuthenticatedUser;
import com.test.ludence.common.page.PageRequest;
import com.test.ludence.post.dto.response.PostPageResponse;
import com.test.ludence.user.dto.response.UserResponse;
import com.test.ludence.user.service.UserPostQueryService;
import com.test.ludence.user.service.UserService;
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
}
