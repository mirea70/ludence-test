package com.test.ludence.post.controller;

import com.test.ludence.auth.security.AuthenticatedUser;
import com.test.ludence.post.dto.request.PostCreateRequest;
import com.test.ludence.post.dto.request.PostUpdateRequest;
import com.test.ludence.post.dto.response.PostIdResponse;
import com.test.ludence.post.dto.response.PostResponse;
import com.test.ludence.post.service.PostCreateService;
import com.test.ludence.post.service.PostDeleteService;
import com.test.ludence.post.service.PostImageService;
import com.test.ludence.post.service.PostQueryService;
import com.test.ludence.post.service.PostUpdateService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostCreateService postCreateService;
    private final PostDeleteService postDeleteService;
    private final PostImageService postImageService;
    private final PostQueryService postQueryService;
    private final PostUpdateService postUpdateService;

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

    @GetMapping("/{id}")
    public ResponseEntity<PostResponse> getPost(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long currentUserId = user == null ? null : user.id();
        return ResponseEntity.ok(postQueryService.getPost(id, currentUserId));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<PostIdResponse> updatePost(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user,
            @Valid @RequestBody PostUpdateRequest request
    ) {
        return ResponseEntity.ok(postUpdateService.updatePost(user.id(), id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        postDeleteService.deletePost(user.id(), id);
        return ResponseEntity.noContent().build();
    }
}
