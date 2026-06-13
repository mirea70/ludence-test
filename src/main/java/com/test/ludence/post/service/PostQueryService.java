package com.test.ludence.post.service;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.common.error.info.PostErrorInfo;
import com.test.ludence.post.dto.response.PostDetailResponse;
import com.test.ludence.post.dto.response.PostResponse;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.post.domain.event.PostViewedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PostResponse getPost(Long postId, Long currentUserId) {
        PostDetailResponse post = postRepository.findActiveDetailById(postId, currentUserId)
                .orElseThrow(() -> new BusinessException(PostErrorInfo.NOT_FOUND));
        eventPublisher.publishEvent(new PostViewedEvent(postId, currentUserId));
        return new PostResponse(post);
    }
}
