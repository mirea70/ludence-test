package com.test.ludens.debug.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.debug.dto.response.DebugPostsResponse;
import com.test.ludens.debug.dto.response.DebugRawResponse;
import com.test.ludens.debug.dto.response.DebugUsersResponse;
import com.test.ludens.post.domain.entity.Post;
import com.test.ludens.common.error.info.PostErrorInfo;
import com.test.ludens.post.repository.PostRepository;
import com.test.ludens.user.domain.entity.User;
import com.test.ludens.common.error.info.UserErrorInfo;
import com.test.ludens.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DebugService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ObjectMapper objectMapper;

    public DebugUsersResponse getAllUsers() {
        return new DebugUsersResponse(userRepository.findAllActiveDetails());
    }

    public DebugPostsResponse getAllPosts() {
        return new DebugPostsResponse(postRepository.findAllActiveDetails());
    }

    public DebugRawResponse getUserRaw(String username) {
        User user = userRepository.findByUsernameValueAndDeletedAtIsNull(username)
                .orElseThrow(() -> new BusinessException(UserErrorInfo.NOT_FOUND));
        return new DebugRawResponse(writeJson(user));
    }

    public DebugRawResponse getPostRaw(Long postId) {
        Post post = postRepository.findByIdAndDeletedAtIsNull(postId)
                .orElseThrow(() -> new BusinessException(PostErrorInfo.NOT_FOUND));
        return new DebugRawResponse(writeJson(post));
    }

    private String writeJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Failed to serialize debug data.", exception);
        }
    }
}
