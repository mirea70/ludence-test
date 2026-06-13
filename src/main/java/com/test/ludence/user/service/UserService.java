package com.test.ludence.user.service;

import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.common.error.info.UserErrorInfo;
import com.test.ludence.user.dto.response.UserDetailResponse;
import com.test.ludence.user.dto.response.UserResponse;
import com.test.ludence.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;

    public UserResponse getUser(String username) {
        UserDetailResponse user = userRepository.findActiveDetailByUsername(username)
                .orElseThrow(() -> new BusinessException(UserErrorInfo.NOT_FOUND));
        return new UserResponse(user);
    }
}
