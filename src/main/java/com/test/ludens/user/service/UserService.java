package com.test.ludens.user.service;

import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.common.error.info.UserErrorInfo;
import com.test.ludens.user.dto.response.UserDetailResponse;
import com.test.ludens.user.dto.response.UserResponse;
import com.test.ludens.user.repository.UserRepository;
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
