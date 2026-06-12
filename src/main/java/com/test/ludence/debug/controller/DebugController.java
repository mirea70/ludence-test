package com.test.ludence.debug.controller;

import com.test.ludence.debug.dto.response.DebugHealthResponse;
import com.test.ludence.debug.dto.response.DebugPostsResponse;
import com.test.ludence.debug.dto.response.DebugRawResponse;
import com.test.ludence.debug.dto.response.DebugUsersResponse;
import com.test.ludence.debug.service.DebugService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/debug")
public class DebugController {

    private final DebugService debugService;

    @GetMapping("/health")
    public ResponseEntity<DebugHealthResponse> getHealth() {
        return ResponseEntity.ok(new DebugHealthResponse("ok"));
    }

    @GetMapping("/allusers")
    public ResponseEntity<DebugUsersResponse> getAllUsers() {
        return ResponseEntity.ok(debugService.getAllUsers());
    }

    @GetMapping("/allposts")
    public ResponseEntity<DebugPostsResponse> getAllPosts() {
        return ResponseEntity.ok(debugService.getAllPosts());
    }

    @GetMapping("/userraw/{username}")
    public ResponseEntity<DebugRawResponse> getUserRaw(@PathVariable String username) {
        return ResponseEntity.ok(debugService.getUserRaw(username));
    }

    @GetMapping("/postraw/{postid}")
    public ResponseEntity<DebugRawResponse> getPostRaw(@PathVariable Long postid) {
        return ResponseEntity.ok(debugService.getPostRaw(postid));
    }
}
