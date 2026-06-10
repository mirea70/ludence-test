package com.test.ludence.post.controller;

import com.test.ludence.auth.security.AuthenticatedUser;
import com.test.ludence.post.dto.request.PostCreateRequest;
import com.test.ludence.post.dto.response.PostIdResponse;
import com.test.ludence.post.service.PostCreateService;
import com.test.ludence.post.service.PostImageService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostCreateService postCreateService;
    private final PostImageService postImageService;

    @PostMapping
    public ResponseEntity<PostIdResponse> createPost(
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @ModelAttribute PostCreateRequest request
    ) {
        PostIdResponse response = postCreateService.createPost(user.id(), request);
        return ResponseEntity.created(URI.create("/posts/" + response.id())).body(response);
    }

    @GetMapping(value = "/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> getPostImage(@PathVariable Long id) {
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .body(postImageService.getImage(id));
    }
}
