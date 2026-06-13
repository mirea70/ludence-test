package com.test.ludence.user.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.test.ludence.recommendation.service.RecommendationRefreshService;
import com.test.ludence.user.domain.vo.UserPostViewId;
import com.test.ludence.user.domain.vo.UserSearchKeywordId;
import com.test.ludence.user.repository.UserPostViewRepository;
import com.test.ludence.user.repository.UserSearchKeywordRepository;
import com.test.ludence.user.repository.UserRepository;
import com.test.ludence.user.domain.entity.User;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("UserActivityService 테스트")
@ExtendWith(MockitoExtension.class)
class UserActivityServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-12T10:00:00Z");
    private static final Clock CLOCK = Clock.fixed(NOW, ZoneOffset.UTC);

    @Mock
    private UserPostViewRepository userPostViewRepository;

    @Mock
    private UserSearchKeywordRepository userSearchKeywordRepository;

    @Mock
    private RecommendationRefreshService recommendationRefreshService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("로그인 사용자가 포스트를 조회하면 조회 수와 사용자 조회 이력 및 갱신 버전을 기록한다")
    void recordsPostViewAndRequestsRefresh_whenUserViewsPost() {
        // given
        given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(user()));
        UserPostViewId excessId = new UserPostViewId(1L, 9L);
        given(userPostViewRepository.findIdsExceedingLimitByUserId(1L, 100)).willReturn(List.of(excessId));
        UserActivityService service = service();

        // when
        service.recordPostView(10L, 1L);

        // then
        verify(userPostViewRepository).upsert(1L, 10L, NOW);
        verify(userPostViewRepository).deleteAllByIdInBatch(List.of(excessId));
        verify(recommendationRefreshService).requestRefresh(1L);
    }

    @Test
    @DisplayName("로그인 사용자가 검색하면 검색 이력과 갱신 버전을 기록한다")
    void recordsSearchAndRequestsRefresh_whenUserSearches() {
        // given
        given(userRepository.findByIdAndDeletedAtIsNull(1L)).willReturn(Optional.of(user()));
        UserSearchKeywordId excessId = new UserSearchKeywordId(1L, "old");
        given(userSearchKeywordRepository.findIdsExceedingLimitByUserId(1L, 20)).willReturn(List.of(excessId));
        UserActivityService service = service();

        // when
        service.recordSearch(1L, "spring");

        // then
        verify(userSearchKeywordRepository).upsert(1L, "spring", NOW);
        verify(userSearchKeywordRepository).deleteAllByIdInBatch(List.of(excessId));
        verify(recommendationRefreshService).requestRefresh(1L);
    }

    private UserActivityService service() {
        return new UserActivityService(
                userPostViewRepository,
                userSearchKeywordRepository,
                userRepository,
                recommendationRefreshService,
                CLOCK
        );
    }

    private User user() {
        return User.create("viewer", "encoded-password", NOW);
    }
}
