package com.test.ludence.post.service;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.common.error.info.PostErrorInfo;
import com.test.ludence.post.dto.request.PostUpdateRequest;
import com.test.ludence.post.dto.response.PostIdResponse;
import com.test.ludence.post.repository.PostRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostUpdateService {

    private final PostRepository postRepository;
    private final Clock clock;

    @Transactional
    public PostIdResponse updatePost(Long authorId, Long postId, PostUpdateRequest request) {
        Post post = postRepository.findActiveByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(PostErrorInfo.NOT_FOUND));
        post.patchByAuthor(authorId, request.title(), request.description(), clock.instant());
        return new PostIdResponse(post.getId());
    }
}
