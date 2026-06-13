package com.test.ludence.user.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import com.test.ludence.user.domain.entity.User;
import com.test.ludence.user.domain.entity.UserPostView;
import com.test.ludence.user.domain.entity.UserSearchKeyword;
import com.test.ludence.user.domain.vo.UserPostViewId;
import com.test.ludence.user.domain.vo.UserSearchKeywordId;
import com.test.ludence.user.repository.UserPostViewRepository;
import com.test.ludence.user.repository.UserRepository;
import com.test.ludence.user.repository.UserSearchKeywordRepository;
import java.time.Instant;
import java.util.stream.LongStream;
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
    private UserSearchKeywordRepository userSearchKeywordRepository;

    @Autowired
    private RecommendationStateRepository recommendationStateRepository;

    private Long userId;

    @AfterEach
    void tearDown() {
        if (userId == null) {
            return;
        }
        userPostViewRepository.deleteAllInBatch();
        userSearchKeywordRepository.deleteAllInBatch();
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

    @Test
    @DisplayName("행동 이력을 기록할 때 사용자별 개수 제한으로 기존 이력을 삭제하지 않는다")
    void retainsActivitiesWithoutPerUserLimit_whenRecordingActivity() {
        // given
        Instant now = Instant.now();
        User user = userRepository.saveAndFlush(User.create("history_viewer", "encoded-password", now));
        userId = user.getId();
        userPostViewRepository.saveAllAndFlush(LongStream.rangeClosed(1, 100)
                .mapToObj(postId -> UserPostView.create(userId, postId, now.minusSeconds(200 - postId)))
                .toList());
        userSearchKeywordRepository.saveAllAndFlush(LongStream.rangeClosed(1, 20)
                .mapToObj(index -> UserSearchKeyword.create(
                        userId,
                        "keyword_" + index,
                        now.minusSeconds(200 - index)
                ))
                .toList());

        // when
        userActivityService.recordPostView(101L, userId);
        userActivityService.recordSearch(userId, "new_keyword");

        // then
        assertThat(userPostViewRepository.count()).isEqualTo(101);
        assertThat(userPostViewRepository.findById(new UserPostViewId(userId, 1L))).isPresent();
        assertThat(userPostViewRepository.findById(new UserPostViewId(userId, 101L))).isPresent();
        assertThat(userSearchKeywordRepository.count()).isEqualTo(21);
        assertThat(userSearchKeywordRepository.findById(new UserSearchKeywordId(userId, "keyword_1"))).isPresent();
        assertThat(userSearchKeywordRepository.findById(new UserSearchKeywordId(userId, "new_keyword"))).isPresent();
    }
}
