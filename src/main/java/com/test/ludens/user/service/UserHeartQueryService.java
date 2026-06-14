package com.test.ludens.user.service;

import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.common.page.PageRequest;
import com.test.ludens.post.dto.response.PostDetailResponse;
import com.test.ludens.post.dto.response.PostPageResponse;
import com.test.ludens.post.repository.PostRepository;
import com.test.ludens.common.error.info.UserErrorInfo;
import com.test.ludens.user.repository.UserRepository;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserHeartQueryService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public PostPageResponse getUserHearts(Long currentUserId, String username, int page, int limit) {
        PageRequest pageRequest = new PageRequest(page, limit);
        Long userId = userRepository.findActiveIdByUsername(username)
                .orElseThrow(() -> new BusinessException(UserErrorInfo.NOT_FOUND));
        if (!Objects.equals(userId, currentUserId)) {
            throw new BusinessException(UserErrorInfo.FORBIDDEN);
        }

        List<PostDetailResponse> posts = postRepository.findActiveDetailsHeartedByUserId(userId, pageRequest);
        long total = postRepository.countActiveHeartedByUserId(userId);
        return new PostPageResponse(page, limit, total, posts);
    }
}
