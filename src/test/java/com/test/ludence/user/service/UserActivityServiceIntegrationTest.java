package com.test.ludence.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import com.test.ludence.user.domain.entity.User;
import com.test.ludence.user.domain.vo.UserPostViewId;
import com.test.ludence.user.repository.UserPostViewRepository;
import com.test.ludence.user.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("UserActivityService 통합 테스트")
class UserActivityServiceIntegrationTest {

    @Autowired
    private UserActivityService userActivityService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserPostViewRepository userPostViewRepository;

    @Autowired
    private RecommendationStateRepository recommendationStateRepository;

    private Long userId;

    @AfterEach
    void tearDown() {
        if (userId == null) {
            return;
        }
        userPostViewRepository.deleteById(new UserPostViewId(userId, 10L));
        recommendationStateRepository.deleteById(userId);
        userRepository.deleteById(userId);
    }

    @Test
    @DisplayName("추천 갱신 상태가 없는 기존 사용자의 포스트 조회 이력도 저장한다")
    void recordsPostView_whenExistingUserHasNoRecommendationState() {
        // given
        User user = userRepository.saveAndFlush(
                User.create("legacy_viewer", "encoded-password", Instant.parse("2026-06-12T10:00:00Z"))
        );
        userId = user.getId();

        // when
        userActivityService.recordPostView(10L, userId);

        // then
        assertThat(userPostViewRepository.findById(new UserPostViewId(userId, 10L))).isPresent();
        assertThat(recommendationStateRepository.findById(userId)).isPresent();
    }
}
