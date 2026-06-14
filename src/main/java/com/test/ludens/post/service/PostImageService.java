package com.test.ludens.post.service;

import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.common.storage.ImageStorage;
import com.test.ludens.common.error.info.PostErrorInfo;
import com.test.ludens.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostImageService {

    private final PostRepository postRepository;
    private final ImageStorage imageStorage;

    public Resource getImage(Long postId) {
        String imageKey = postRepository.findActiveImageKeyById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorInfo.NOT_FOUND));
        return imageStorage.get(imageKey);
    }
}
