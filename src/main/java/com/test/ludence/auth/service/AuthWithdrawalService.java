package com.test.ludence.auth.service;

import com.test.ludence.auth.domain.info.AuthErrorInfo;
import com.test.ludence.auth.security.AnonymousUsernameGenerator;
import com.test.ludence.common.error.exception.BusinessException;
import com.test.ludence.heart.domain.info.HeartErrorInfo;
import com.test.ludence.heart.repository.HeartCountByPostId;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.user.domain.entity.User;
import com.test.ludence.user.repository.UserRepository;
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
