package com.test.ludence.heart.service;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.heart.domain.info.HeartErrorInfo;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.domain.info.PostErrorInfo;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.recommendation.service.RecommendationRefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HeartDeleteService {

    private final PostRepository postRepository;
    private final HeartRepository heartRepository;
    private final PostHeartCountRepository postHeartCountRepository;
    private final RecommendationRefreshService recommendationRefreshService;

    @Transactional
    public void deleteHeart(Long userId, Long postId) {
        recommendationRefreshService.requestRefresh(userId);
        PostHeartCount heartCount = postHeartCountRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(PostErrorInfo.NOT_FOUND));
        if (!postRepository.existsByIdAndDeletedAtIsNull(postId)) {
            throw new BusinessException(PostErrorInfo.NOT_FOUND);
        }
        if (heartRepository.deleteByUserIdAndPostId(userId, postId) == 0) {
            throw new BusinessException(HeartErrorInfo.NOT_FOUND);
        }
        heartCount.decrement();
    }
}
