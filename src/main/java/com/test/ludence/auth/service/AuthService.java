package com.test.ludence.auth.service;

import com.test.ludence.auth.domain.info.AuthErrorInfo;
import com.test.ludence.auth.dto.request.AuthRequest;
import com.test.ludence.auth.security.JwtTokenProvider;
import com.test.ludence.auth.security.PasswordHasher;
import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.user.domain.entity.User;
import com.test.ludence.user.repository.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtTokenProvider jwtTokenProvider;
    private final Clock clock;

    @Transactional
    public String signup(AuthRequest request) {
        validateUsernameAvailable(request.username());

        User user = User.create(
                request.username(),
                passwordHasher.hash(request.password()),
                clock.instant()
        );
        return jwtTokenProvider.createToken(save(user));
    }

    public String login(AuthRequest request) {
        User user = userRepository.findByUsernameValueAndDeletedAtIsNull(request.username())
                .filter(found -> passwordHasher.matches(request.password(), found.getPassword()))
                .orElseThrow(() -> new BusinessException(AuthErrorInfo.INVALID_CREDENTIALS));

        return jwtTokenProvider.createToken(user);
    }

    private void validateUsernameAvailable(String username) {
        if (userRepository.existsByUsernameValue(username)) {
            throw new BusinessException(AuthErrorInfo.DUPLICATED_USERNAME);
        }
    }

    private User save(User user) {
        try {
            return userRepository.saveAndFlush(user);
        } catch (DataIntegrityViolationException exception) {
            throw new BusinessException(AuthErrorInfo.DUPLICATED_USERNAME);
        }
    }
}
