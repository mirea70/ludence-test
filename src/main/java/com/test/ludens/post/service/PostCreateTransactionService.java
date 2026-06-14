package com.test.ludens.post.service;

import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.common.storage.ImageStorage;
import com.test.ludens.common.storage.StagedImage;
import com.test.ludens.heart.domain.entity.PostHeartCount;
import com.test.ludens.heart.repository.PostHeartCountRepository;
import com.test.ludens.post.domain.entity.Post;
import com.test.ludens.post.domain.entity.PostViewCount;
import com.test.ludens.post.dto.response.PostIdResponse;
import com.test.ludens.post.repository.PostRepository;
import com.test.ludens.post.repository.PostViewCountRepository;
import com.test.ludens.common.error.info.UserErrorInfo;
import com.test.ludens.user.repository.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostCreateTransactionService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final PostHeartCountRepository postHeartCountRepository;
    private final PostViewCountRepository postViewCountRepository;
    private final ImageStorage imageStorage;
    private final Clock clock;

    @Transactional
    public PostIdResponse createPost(Long authorId, String title, String description, StagedImage stagedImage) {
        userRepository.findActiveByIdForUpdate(authorId)
                .orElseThrow(() -> new BusinessException(UserErrorInfo.NOT_FOUND));

        Post post = postRepository.saveAndFlush(
                Post.create(authorId, title, description, stagedImage.key(), clock.instant())
        );
        postHeartCountRepository.saveAndFlush(PostHeartCount.create(post.getId()));
        postViewCountRepository.saveAndFlush(PostViewCount.create(post.getId()));

        imageStorage.commit(stagedImage);
        return new PostIdResponse(post.getId());
    }
}
