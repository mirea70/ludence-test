package com.test.ludens.auth.service;

import com.test.ludens.common.error.info.AuthErrorInfo;
import com.test.ludens.auth.dto.request.AuthRequest;
import com.test.ludens.auth.dto.response.TokenResponse;
import com.test.ludens.auth.security.provider.JwtTokenProvider;
import com.test.ludens.auth.security.hasher.PasswordHasher;
import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.recommendation.domain.entity.RecommendationState;
import com.test.ludens.recommendation.repository.RecommendationStateRepository;
import com.test.ludens.user.domain.entity.User;
import com.test.ludens.user.repository.UserRepository;
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
    private final RecommendationStateRepository recommendationStateRepository;
    private final Clock clock;

    @Transactional
    public TokenResponse signup(AuthRequest request) {
        validateUsernameAvailable(request.username());

        User user = User.create(
                request.username(),
                passwordHasher.hash(request.password()),
                clock.instant()
        );
        User savedUser = save(user);
        recommendationStateRepository.save(RecommendationState.create(savedUser.getId()));
        return new TokenResponse(jwtTokenProvider.createToken(savedUser));
    }

    public TokenResponse login(AuthRequest request) {
        User user = userRepository.findByUsernameValueAndDeletedAtIsNull(request.username())
                .filter(found -> passwordHasher.matches(request.password(), found.getPassword()))
                .orElseThrow(() -> new BusinessException(AuthErrorInfo.INVALID_CREDENTIALS));

        return new TokenResponse(jwtTokenProvider.createToken(user));
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
