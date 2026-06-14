package com.test.ludens.heart.service;

import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.heart.domain.entity.Heart;
import com.test.ludens.common.error.info.HeartErrorInfo;
import com.test.ludens.heart.repository.HeartRepository;
import com.test.ludens.heart.repository.PostHeartCountRepository;
import com.test.ludens.common.error.info.PostErrorInfo;
import com.test.ludens.post.repository.PostRepository;
import com.test.ludens.recommendation.service.RecommendationRefreshService;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class HeartCreateService {

    private final PostRepository postRepository;
    private final HeartRepository heartRepository;
    private final PostHeartCountRepository postHeartCountRepository;
    private final RecommendationRefreshService recommendationRefreshService;

    @Transactional
    public void createHeart(Long userId, Long postId) {
        recommendationRefreshService.requestRefresh(userId);

        increaseHeartCount(postId);
        validatePostExists(postId);
        saveData(userId, postId);
    }

    private void increaseHeartCount(Long postId) {
        if (postHeartCountRepository.increase(postId) != 1) {
            throw new BusinessException(HeartErrorInfo.NOT_FOUND_COUNT);
        }
    }

    private void validatePostExists(Long postId) {
        if (!postRepository.existsByIdAndDeletedAtIsNull(postId)) {
            throw new BusinessException(PostErrorInfo.NOT_FOUND);
        }
    }

    private void saveData(Long userId, Long postId) {
        try {
            heartRepository.saveAndFlush(Heart.create(userId, postId));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(HeartErrorInfo.ALREADY_EXISTS);
        }
    }
}
