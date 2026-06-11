package com.test.ludence.heart.service;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.heart.domain.entity.Heart;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.heart.domain.info.HeartErrorInfo;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.domain.info.PostErrorInfo;
import com.test.ludence.post.repository.PostRepository;
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

    @Transactional
    public void createHeart(Long userId, Long postId) {
        PostHeartCount heartCount = postHeartCountRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new BusinessException(PostErrorInfo.NOT_FOUND));
        if (!postRepository.existsByIdAndDeletedAtIsNull(postId)) {
            throw new BusinessException(PostErrorInfo.NOT_FOUND);
        }
        saveHeart(userId, postId);
        heartCount.increment();
    }

    private void saveHeart(Long userId, Long postId) {
        try {
            heartRepository.saveAndFlush(Heart.create(userId, postId));
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(HeartErrorInfo.ALREADY_EXISTS);
        }
    }
}
