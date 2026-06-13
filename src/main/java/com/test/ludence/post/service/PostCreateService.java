package com.test.ludence.post.service;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.post.domain.info.PostErrorInfo;
import com.test.ludence.post.dto.request.PostCreateRequest;
import com.test.ludence.post.dto.response.PostIdResponse;
import com.test.ludence.common.storage.ImageStorage;
import com.test.ludence.common.storage.StagedImage;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamSource;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PostCreateService {

    private final PostCreateTransactionService transactionService;
    private final ImageStorage imageStorage;

    public PostIdResponse createPost(Long authorId, PostCreateRequest request) {
        StagedImage stagedImage = stage(request.image());
        try {
            return transactionService.createPost(authorId, request.title(), request.description(), stagedImage);
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
