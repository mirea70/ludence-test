package com.test.ludens.user.service;

import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.common.page.PageRequest;
import com.test.ludens.post.dto.response.PostDetailResponse;
import com.test.ludens.post.dto.response.PostPageResponse;
import com.test.ludens.post.repository.PostRepository;
import com.test.ludens.common.error.info.UserErrorInfo;
import com.test.ludens.user.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserPostQueryService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PostPageResponse getUserPosts(String username, int page, int limit, Long currentUserId) {
        PageRequest pageRequest = new PageRequest(page, limit);
        Long authorId = getActiveUserId(username);

        List<PostDetailResponse> posts = postRepository.findActiveDetailsByAuthorId(
                authorId,
                username,
                currentUserId,
                pageRequest
        );
        long total = postRepository.countActiveByAuthorId(authorId);
        return new PostPageResponse(page, limit, total, posts);
    }

    private Long getActiveUserId(String username) {
        return userRepository.findActiveIdByUsername(username)
                .orElseThrow(() -> new BusinessException(UserErrorInfo.NOT_FOUND));
    }
}
