package com.test.ludence.heart.service;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.common.page.PageRequest;
import com.test.ludence.heart.dto.response.HeartUserPageResponse;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.common.error.info.PostErrorInfo;
import com.test.ludence.post.repository.PostHeartAccess;
import com.test.ludence.post.repository.PostRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class HeartQueryService {

    private final PostRepository postRepository;
    private final HeartRepository heartRepository;

    public HeartUserPageResponse getPostHearts(Long currentUserId, Long postId, int page, int limit) {
        PageRequest pageRequest = new PageRequest(page, limit);
        PostHeartAccess post = postRepository.findActiveHeartAccessById(postId)
                .orElseThrow(() -> new BusinessException(PostErrorInfo.NOT_FOUND));
        if (!Objects.equals(post.authorId(), currentUserId)) {
            throw new BusinessException(PostErrorInfo.FORBIDDEN);
        }

        List<String> users = heartRepository.findActiveUsernamesByPostId(postId, pageRequest);
        return new HeartUserPageResponse(page, limit, post.total(), users);
    }
}
