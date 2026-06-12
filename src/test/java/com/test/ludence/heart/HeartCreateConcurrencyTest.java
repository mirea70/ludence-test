package com.test.ludence.heart;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.heart.domain.entity.Heart;
import com.test.ludence.heart.domain.entity.HeartId;
import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.heart.service.HeartCreateService;
import com.test.ludence.heart.service.HeartDeleteService;
import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.post.service.PostDeleteService;
import com.test.ludence.recommendation.domain.entity.RecommendationState;
import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import java.time.Instant;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.LongStream;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("하트 생성 동시성 테스트")
class HeartCreateConcurrencyTest {

    private static final int REQUEST_COUNT = 20;

    @Autowired
    private HeartCreateService heartCreateService;

    @Autowired
    private HeartDeleteService heartDeleteService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private PostDeleteService postDeleteService;

    @Autowired
    private HeartRepository heartRepository;

    @Autowired
    private PostHeartCountRepository postHeartCountRepository;

    @Autowired
    private RecommendationStateRepository recommendationStateRepository;

    @AfterEach
    void cleanUp() {
        heartRepository.deleteAll();
        postHeartCountRepository.deleteAll();
        postRepository.deleteAll();
        recommendationStateRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 회원의 동시 하트 요청에도 하트와 집계값은 하나만 생성된다")
    void createsOneHeartAndCount_whenSameUserRequestsConcurrently() throws Exception {
        // given
        Post post = postRepository.save(Post.create(
                1L,
                "title",
                null,
                "550e8400-e29b-41d4-a716-446655440000.png",
                Instant.parse("2026-06-10T10:00:00Z")
        ));
        postHeartCountRepository.save(PostHeartCount.create(post.getId()));
        createRecommendationStates(1L);
        AtomicInteger successCount = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(REQUEST_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);

        // when
        for (int i = 0; i < REQUEST_COUNT; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    heartCreateService.createHeart(1L, post.getId());
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                }
            });
        }
        ready.await();
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(heartRepository.existsById(new HeartId(1L, post.getId()))).isTrue();
        assertThat(heartRepository.count()).isEqualTo(1);
        assertThat(postHeartCountRepository.findById(post.getId()).orElseThrow().getCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("서로 다른 회원의 동시 하트 요청에도 모든 하트와 집계값이 반영된다")
    void createsAllHeartsAndCount_whenDifferentUsersRequestConcurrently() throws Exception {
        // given
        Post post = postRepository.save(Post.create(
                1L,
                "title",
                null,
                "550e8400-e29b-41d4-a716-446655440001.png",
                Instant.parse("2026-06-10T10:00:00Z")
        ));
        postHeartCountRepository.save(PostHeartCount.create(post.getId()));
        createRecommendationStates(LongStream.rangeClosed(1, REQUEST_COUNT).toArray());
        CountDownLatch ready = new CountDownLatch(REQUEST_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);

        // when
        LongStream.rangeClosed(1, REQUEST_COUNT).forEach(userId -> executor.submit(() -> {
            ready.countDown();
            try {
                start.await();
                heartCreateService.createHeart(userId, post.getId());
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
        }));
        ready.await();
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        // then
        assertThat(heartRepository.count()).isEqualTo(REQUEST_COUNT);
        assertThat(postHeartCountRepository.findById(post.getId()).orElseThrow().getCount())
                .isEqualTo(REQUEST_COUNT);
    }

    @Test
    @DisplayName("하트 추가와 포스트 삭제가 동시에 실행되어도 삭제 후 하트와 집계값은 남지 않는다")
    void leavesNoHeartAndCount_whenHeartCreationRacesWithPostDeletion() throws Exception {
        // given
        Post post = postRepository.save(Post.create(
                1L,
                "title",
                null,
                UUID.randomUUID() + ".png",
                Instant.parse("2026-06-10T10:00:00Z")
        ));
        postHeartCountRepository.save(PostHeartCount.create(post.getId()));
        createRecommendationStates(2L);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        // when
        executor.submit(() -> executeConcurrently(ready, start, () -> heartCreateService.createHeart(2L, post.getId())));
        executor.submit(() -> executeConcurrently(ready, start, () -> postDeleteService.deletePost(1L, post.getId())));
        ready.await();
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        // then
        assertThat(postRepository.findById(post.getId()).orElseThrow().isActive()).isFalse();
        assertThat(heartRepository.count()).isZero();
        assertThat(postHeartCountRepository.findById(post.getId()).orElseThrow().getCount()).isZero();
    }

    @Test
    @DisplayName("같은 하트를 동시에 삭제해도 한 요청만 성공하고 집계값은 0이 된다")
    void deletesOneHeartAndCount_whenSameHeartIsDeletedConcurrently() throws Exception {
        // given
        Post post = postRepository.save(Post.create(
                1L,
                "title",
                null,
                UUID.randomUUID() + ".png",
                Instant.parse("2026-06-10T10:00:00Z")
        ));
        PostHeartCount heartCount = PostHeartCount.create(post.getId());
        heartCount.increment();
        postHeartCountRepository.save(heartCount);
        heartRepository.save(Heart.create(2L, post.getId()));
        createRecommendationStates(2L);
        AtomicInteger successCount = new AtomicInteger();
        CountDownLatch ready = new CountDownLatch(REQUEST_COUNT);
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(REQUEST_COUNT);

        // when
        for (int i = 0; i < REQUEST_COUNT; i++) {
            executor.submit(() -> {
                ready.countDown();
                try {
                    start.await();
                    heartDeleteService.deleteHeart(2L, post.getId());
                    successCount.incrementAndGet();
                } catch (Exception ignored) {
                }
            });
        }
        ready.await();
        start.countDown();
        executor.shutdown();
        assertThat(executor.awaitTermination(10, TimeUnit.SECONDS)).isTrue();

        // then
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(heartRepository.existsById(new HeartId(2L, post.getId()))).isFalse();
        assertThat(postHeartCountRepository.findById(post.getId()).orElseThrow().getCount()).isZero();
    }

    private void executeConcurrently(CountDownLatch ready, CountDownLatch start, Runnable task) {
        ready.countDown();
        try {
            start.await();
            task.run();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
        } catch (RuntimeException ignored) {
        }
    }

    private void createRecommendationStates(long... userIds) {
        recommendationStateRepository.saveAll(
                LongStream.of(userIds)
                        .mapToObj(RecommendationState::create)
                        .toList()
        );
        recommendationStateRepository.flush();
    }
}
