package com.test.ludence.post.service;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.common.error.info.PostErrorInfo;
import com.test.ludence.post.repository.PostRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostDeleteService {

    private final PostRepository postRepository;
    private final HeartRepository heartRepository;
    private final PostHeartCountRepository postHeartCountRepository;
    private final Clock clock;

    @Transactional
    public void deletePost(Long authorId, Long postId) {
        PostHeartCount heartCount = postHeartCountRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(PostErrorInfo.NOT_FOUND));
        Post post = postRepository.findActiveByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(PostErrorInfo.NOT_FOUND));
        post.deleteByAuthor(authorId, clock.instant());

        heartRepository.deleteByPostId(postId);
        heartCount.reset();
    }
}
