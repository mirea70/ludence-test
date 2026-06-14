package com.test.ludens.user;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludens.support.IntegrationTestSupport;
import com.test.ludens.user.domain.entity.UserPostView;
import com.test.ludens.user.domain.entity.UserSearchKeyword;
import com.test.ludens.user.repository.UserPostViewRepository;
import com.test.ludens.user.repository.UserSearchKeywordRepository;
import com.test.ludens.user.service.UserActivityCleanupService;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

@DisplayName("사용자 행동 이력 정리 통합 테스트")
class UserActivityCleanupIntegrationTest extends IntegrationTestSupport {

    private static final Instant NOW = Instant.parse("2026-06-12T10:00:00Z");

    @Autowired
    private UserActivityCleanupService cleanupService;

    @Autowired
    private UserPostViewRepository userPostViewRepository;

    @Autowired
    private UserSearchKeywordRepository userSearchKeywordRepository;

    @Test
    @DisplayName("정리 시 7일이 지난 조회와 검색 이력만 삭제한다")
    void deletesOnlyExpiredActivities() {
        // given
        userPostViewRepository.save(UserPostView.create(1L, 1L, NOW.minusSeconds(604801)));
        userSearchKeywordRepository.save(UserSearchKeyword.create(1L, "expired", NOW.minusSeconds(604801)));
        for (long index = 1; index <= 101; index++) {
            userPostViewRepository.save(UserPostView.create(1L, index + 1, NOW.minusSeconds(1000 - index)));
        }
        for (int index = 1; index <= 21; index++) {
            userSearchKeywordRepository.save(UserSearchKeyword.create(
                    1L,
                    "keyword_" + index,
                    NOW.minusSeconds(1000 - index)
            ));
        }

        // when
        cleanupService.cleanup();

        // then
        assertThat(userPostViewRepository.count()).isEqualTo(101);
        assertThat(userSearchKeywordRepository.count()).isEqualTo(21);
    }
}
