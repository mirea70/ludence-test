package com.test.ludence.search.controller;

import com.test.ludence.auth.security.dto.AuthenticatedUser;
import com.test.ludence.common.page.PageRequest;
import com.test.ludence.post.dto.response.PostPageResponse;
import com.test.ludence.search.service.PostSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/search")
public class SearchController {

    private final PostSearchService postSearchService;

    @GetMapping("/posts")
    public ResponseEntity<PostPageResponse> searchPosts(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "" + PageRequest.DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + PageRequest.DEFAULT_LIMIT) int limit,
            @AuthenticationPrincipal AuthenticatedUser user
    ) {
        Long currentUserId = user == null ? null : user.id();
        return ResponseEntity.ok(postSearchService.searchPosts(q, page, limit, currentUserId));
    }
}
