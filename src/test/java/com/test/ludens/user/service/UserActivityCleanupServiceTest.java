package com.test.ludens.user.service;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.test.ludens.user.domain.vo.UserPostViewId;
import com.test.ludens.user.domain.vo.UserSearchKeywordId;
import com.test.ludens.user.repository.UserPostViewRepository;
import com.test.ludens.user.repository.UserSearchKeywordRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("UserActivityCleanupService 테스트")
@ExtendWith(MockitoExtension.class)
class UserActivityCleanupServiceTest {

    private static final Instant NOW = Instant.parse("2026-06-12T10:00:00Z");
    private static final Instant EXPIRED_AT = NOW.minusSeconds(604800);

    @Mock
    private UserPostViewRepository userPostViewRepository;
    @Mock
    private UserSearchKeywordRepository userSearchKeywordRepository;

    @Test
    @DisplayName("7일 만료 행동 이력을 배치 삭제한다")
    void deletesExpiredActivities() {
        // given
        UserPostViewId expiredView = new UserPostViewId(1L, 1L);
        UserSearchKeywordId expiredKeyword = new UserSearchKeywordId(1L, "expired");
        given(userPostViewRepository.findIdsLastViewedBefore(EXPIRED_AT)).willReturn(List.of(expiredView));
        given(userSearchKeywordRepository.findIdsLastSearchedBefore(EXPIRED_AT))
                .willReturn(List.of(expiredKeyword));

        // when
        service().cleanup();

        // then
        verify(userPostViewRepository).deleteAllByIdInBatch(List.of(expiredView));
        verify(userSearchKeywordRepository).deleteAllByIdInBatch(List.of(expiredKeyword));
    }

    private UserActivityCleanupService service() {
        return new UserActivityCleanupService(
                userPostViewRepository,
                userSearchKeywordRepository,
                Clock.fixed(NOW, ZoneOffset.UTC)
        );
    }
}
