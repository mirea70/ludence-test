package com.test.ludence.recommendation;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.recommendation.domain.entity.RecommendationState;
import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import com.test.ludence.recommendation.service.RecommendationRefreshService;
import com.test.ludence.user.domain.entity.User;
import com.test.ludence.user.repository.UserRepository;
import java.time.Instant;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("추천 갱신 동시성 테스트")
class RecommendationRefreshConcurrencyTest {

    private static final int REQUEST_COUNT = 20;

    @Autowired
    private RecommendationRefreshService recommendationRefreshService;

    @Autowired
    private RecommendationStateRepository recommendationStateRepository;

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        recommendationStateRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("동일 사용자의 추천 갱신 요청을 동시에 처리해도 모든 요청 버전이 반영된다")
    void incrementsAllRequestedVersions_whenRefreshRequestsAreConcurrent() throws Exception {
        // given
        User user = userRepository.saveAndFlush(User.create(
                "viewer",
                "encoded-password",
                Instant.parse("2026-06-12T10:00:00Z")
        ));
        recommendationStateRepository.saveAndFlush(RecommendationState.create(user.getId()));
        CountDownLatch ready = new CountDownLatch(REQUEST_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);

        // when
        for (int i = 0; i < REQUEST_COUNT; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    recommendationRefreshService.requestRefresh(user.getId());
                } catch (InterruptedException exception) {
                    Thread.currentThread().interrupt();
                }
            });
        }
        ready.await();
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        // then
        RecommendationState state = recommendationStateRepository.findById(user.getId()).orElseThrow();
        assertThat(state.getRequestedVersion()).isEqualTo(REQUEST_COUNT + 1L);
    }
}
