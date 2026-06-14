package com.test.ludens.post.controller;

import com.test.ludens.auth.security.dto.AuthenticatedUser;
import com.test.ludens.common.page.PageRequest;
import com.test.ludens.heart.dto.response.HeartUserPageResponse;
import com.test.ludens.heart.service.HeartCreateService;
import com.test.ludens.heart.service.HeartDeleteService;
import com.test.ludens.heart.service.HeartQueryService;
import com.test.ludens.post.dto.request.PostCreateRequest;
import com.test.ludens.post.dto.request.PostUpdateRequest;
import com.test.ludens.post.dto.response.PostIdResponse;
import com.test.ludens.post.dto.response.PostResponse;
import com.test.ludens.post.service.PostCreateService;
import com.test.ludens.post.service.PostDeleteService;
import com.test.ludens.post.service.PostImageService;
import com.test.ludens.post.service.PostQueryService;
import com.test.ludens.post.service.PostUpdateService;
import jakarta.validation.Valid;
import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/posts")
public class PostController {

    private final PostCreateService postCreateService;
    private final HeartCreateService heartCreateService;
    private final HeartDeleteService heartDeleteService;
    private final HeartQueryService heartQueryService;
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

    @PostMapping("/{id}/heart")
    public ResponseEntity<Void> createHeart(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        heartCreateService.createHeart(user.id(), id);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{id}/heart")
    public ResponseEntity<Void> deleteHeart(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        heartDeleteService.deleteHeart(user.id(), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/hearts")
    public ResponseEntity<HeartUserPageResponse> getPostHearts(
            @PathVariable Long id,
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestParam(defaultValue = "" + PageRequest.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + PageRequest.DEFAULT_LIMIT) int limit
    ) {
        return ResponseEntity.ok(heartQueryService.getPostHearts(user.id(), id, page, limit));
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
