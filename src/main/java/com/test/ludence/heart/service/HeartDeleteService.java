package com.test.ludence.heart.service;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.common.error.info.HeartErrorInfo;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.common.error.info.PostErrorInfo;
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

        decreaseHeartCount(postId);
        validatePostExists(postId);
        deleteData(userId, postId);
    }

    private void decreaseHeartCount(Long postId) {
        if (postHeartCountRepository.decrease(postId, 1) != 1) {
            throw new BusinessException(HeartErrorInfo.NOT_FOUND_COUNT);
        }
    }

    private void validatePostExists(Long postId) {
        if (!postRepository.existsByIdAndDeletedAtIsNull(postId)) {
            throw new BusinessException(PostErrorInfo.NOT_FOUND);
        }
    }

    private void deleteData(Long userId, Long postId) {
        if (heartRepository.deleteByUserIdAndPostId(userId, postId) == 0) {
            throw new BusinessException(HeartErrorInfo.NOT_FOUND);
        }
    }
}
