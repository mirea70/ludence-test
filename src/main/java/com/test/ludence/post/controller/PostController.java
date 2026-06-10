package com.test.ludence.post.controller;

import com.test.ludence.auth.security.AuthenticatedUser;
import com.test.ludence.post.dto.request.PostCreateRequest;
import com.test.ludence.post.dto.response.PostIdResponse;
import com.test.ludence.post.service.PostCreateService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostCreateService postCreateService;

    @PostMapping
    public ResponseEntity<PostIdResponse> createPost(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute PostCreateRequest request
    ) {
        PostIdResponse response = postCreateService.createPost(user.id(), request);
        return ResponseEntity.created(URI.create("/posts/" + response.id())).body(response);
    }
}
