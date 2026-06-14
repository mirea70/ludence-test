package com.test.ludens.auth.service;

import com.test.ludens.common.error.info.AuthErrorInfo;
import com.test.ludens.auth.security.generator.AnonymousUsernameGenerator;
import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.common.error.info.HeartErrorInfo;
import com.test.ludens.heart.repository.HeartCountByPostId;
import com.test.ludens.heart.repository.HeartRepository;
import com.test.ludens.heart.repository.PostHeartCountRepository;
import com.test.ludens.post.repository.PostRepository;
import com.test.ludens.user.domain.entity.User;
import com.test.ludens.user.repository.UserRepository;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthWithdrawalService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final HeartRepository heartRepository;
    private final PostHeartCountRepository postHeartCountRepository;
    private final AnonymousUsernameGenerator anonymousUsernameGenerator;
    private final Clock clock;

    @Transactional
    public void withdraw(Long userId) {
        User user = userRepository.findActiveByIdForUpdate(userId)
                .orElseThrow(() -> new BusinessException(AuthErrorInfo.INVALID_TOKEN));

        postRepository.clearAuthorId(userId);
        for (HeartCountByPostId heartCount : heartRepository.getCountsByUserId(userId)) {
            long updatedCount = postHeartCountRepository.decrease(heartCount.postId(), heartCount.count());
            if (updatedCount != 1) {
                throw new BusinessException(HeartErrorInfo.INVALID_COUNT);
            }
        }
        heartRepository.deleteByUserId(userId);

        user.withdraw(anonymousUsernameGenerator.generate(), clock.instant());
        userRepository.flush();
    }
}
