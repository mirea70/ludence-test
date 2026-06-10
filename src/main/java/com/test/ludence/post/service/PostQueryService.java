package com.test.ludence.post.service;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.post.domain.info.PostErrorInfo;
import com.test.ludence.post.dto.response.PostDetailResponse;
import com.test.ludence.post.dto.response.PostResponse;
import com.test.ludence.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostQueryService {

    private final PostRepository postRepository;

    public PostResponse getPost(Long postId, Long currentUserId) {
        PostDetailResponse post = postRepository.findActiveDetailById(postId, currentUserId)
                .orElseThrow(() -> new BusinessException(PostErrorInfo.NOT_FOUND));
        return new PostResponse(post);
    }
}
