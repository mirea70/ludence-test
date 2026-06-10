package com.test.ludence.post.service;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.post.domain.info.PostErrorInfo;
import com.test.ludence.post.dto.request.PostCreateRequest;
import com.test.ludence.post.dto.response.PostIdResponse;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.common.storage.ImageStorage;
import com.test.ludence.common.storage.StagedImage;
import com.test.ludence.user.domain.info.UserErrorInfo;
import com.test.ludence.user.repository.UserRepository;
import java.io.IOException;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCreateService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostHeartCountRepository postHeartCountRepository;
    private final ImageStorage imageStorage;
    private final Clock clock;

    @Transactional
    public PostIdResponse createPost(Long authorId, PostCreateRequest request) {
        userRepository.findActiveByIdForUpdate(authorId)
                .orElseThrow(() -> new BusinessException(UserErrorInfo.NOT_FOUND));

        StagedImage stagedImage = stage(request.image());
        try {
            Post post = postRepository.saveAndFlush(
                    Post.create(authorId, request.title(), request.description(), stagedImage.key(), clock.instant())
            );
            postHeartCountRepository.saveAndFlush(PostHeartCount.create(post.getId()));
            imageStorage.commit(stagedImage);
            return new PostIdResponse(post.getId());
        } catch (RuntimeException exception) {
            imageStorage.discard(stagedImage);
            throw exception;
        }
    }

    private StagedImage stage(InputStreamSource image) {
        try {
            return imageStorage.stage(image.getInputStream());
        } catch (IOException exception) {
            throw new BusinessException(PostErrorInfo.IMAGE_STORAGE_FAILED);
        }
    }
}
