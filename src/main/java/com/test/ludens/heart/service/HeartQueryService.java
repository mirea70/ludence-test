package com.test.ludens.heart.service;

import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.common.page.PageRequest;
import com.test.ludens.heart.dto.response.HeartUserPageResponse;
import com.test.ludens.heart.repository.HeartRepository;
import com.test.ludens.common.error.info.PostErrorInfo;
import com.test.ludens.post.repository.PostHeartAccess;
import com.test.ludens.post.repository.PostRepository;
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
