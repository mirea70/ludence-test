package com.test.ludens.post;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludens.post.domain.entity.PostViewCount;
import com.test.ludens.post.repository.PostViewCountRepository;
import com.test.ludens.post.service.PostViewCountService;
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
@DisplayName("포스트 조회수 동시성 테스트")
class PostViewCountConcurrencyTest {

    private static final int REQUEST_COUNT = 20;

    @Autowired
    private PostViewCountService postViewCountService;

    @Autowired
    private PostViewCountRepository postViewCountRepository;

    @AfterEach
    void cleanUp() {
        postViewCountRepository.deleteAll();
    }

    @Test
    @DisplayName("동일 포스트를 동시에 조회해도 모든 조회수가 반영된다")
    void incrementsAllViewCounts_whenSamePostIsViewedConcurrently() throws Exception {
        // given
        postViewCountRepository.saveAndFlush(PostViewCount.create(1L));
        CountDownLatch ready = new CountDownLatch(REQUEST_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);

        // when
        for (int index = 0; index < REQUEST_COUNT; index++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    postViewCountService.recordView(1L);
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
        assertThat(postViewCountRepository.findById(1L).orElseThrow().getCount()).isEqualTo(REQUEST_COUNT);
    }
}
