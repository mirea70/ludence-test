package com.test.ludence.recommendation.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.test.ludence.heart.domain.entity.PostHeartCount;
import com.test.ludence.heart.domain.entity.Heart;
import com.test.ludence.post.domain.entity.Post;
import com.test.ludence.post.domain.entity.PostViewCount;
import com.test.ludence.support.JpaTestSupport;
import com.test.ludence.user.domain.entity.UserPostView;
import com.test.ludence.user.domain.entity.UserSearchKeyword;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("RecommendationCandidateQueryRepository 테스트")
class RecommendationCandidateQueryRepositoryTest extends JpaTestSupport {

    @Test
    @DisplayName("공통 추천 후보는 하트 수, 조회 수, 최신 순으로 조회한다")
    void findsCommonCandidatesInPopularityOrder() {
        // given
        Post popular = savePost("popular", Instant.parse("2026-06-10T10:00:00Z"));
        Post viewed = savePost("viewed", Instant.parse("2026-06-11T10:00:00Z"));
        Post latest = savePost("latest", Instant.parse("2026-06-12T10:00:00Z"));
        saveCounts(popular.getId(), 2, 0);
        saveCounts(viewed.getId(), 1, 3);
        saveCounts(latest.getId(), 1, 1);
        entityManager.flush();
        entityManager.clear();

        // when
        List<Long> result = recommendationCandidateQueryRepository.findCommon(40);

        // then
        assertThat(result).containsExactly(popular.getId(), viewed.getId(), latest.getId());
    }

    @Test
    @DisplayName("사용자 추천 후보는 본인 포스트와 이미 하트한 포스트를 제외한다")
    void findsPersonalizedCandidatesExcludingOwnedAndHeartedPosts() {
        // given
        Instant now = Instant.parse("2026-06-12T10:00:00Z");
        Post heartedSource = savePost(2L, "source", now.minusSeconds(100));
        Post sameAuthor = savePost(2L, "same author", now.minusSeconds(90));
        Post viewedSource = savePost(3L, "viewed source", now.minusSeconds(80));
        Post viewedAuthor = savePost(3L, "viewed author", now.minusSeconds(70));
        Post searched = savePost(4L, "spring framework", now.minusSeconds(60));
        Post owned = savePost(1L, "spring owned", now.minusSeconds(50));
        List.of(heartedSource, sameAuthor, viewedSource, viewedAuthor, searched, owned)
                .forEach(post -> saveCounts(post.getId(), 0, 0));
        heartRepository.save(Heart.create(1L, heartedSource.getId()));
        userPostViewRepository.save(UserPostView.create(1L, viewedSource.getId(), now.minusSeconds(10)));
        userSearchKeywordRepository.save(UserSearchKeyword.create(1L, "spring", now.minusSeconds(10)));
        entityManager.flush();
        entityManager.clear();

        // when
        List<Long> heartCandidates = recommendationCandidateQueryRepository.findByHeartedAuthors(1L, 40);
        List<Long> viewCandidates = recommendationCandidateQueryRepository.findByViewedAuthors(
                1L,
                now.minusSeconds(604800),
                40
        );
        List<Long> searchCandidates = recommendationCandidateQueryRepository.findByRecentSearches(
                1L,
                now.minusSeconds(604800),
                40
        );

        // then
        assertThat(heartCandidates).contains(sameAuthor.getId()).doesNotContain(heartedSource.getId(), owned.getId());
        assertThat(viewCandidates).contains(viewedSource.getId(), viewedAuthor.getId()).doesNotContain(owned.getId());
        assertThat(searchCandidates).contains(searched.getId()).doesNotContain(owned.getId(), heartedSource.getId());
    }

    private Post savePost(String title, Instant createdAt) {
        return savePost(1L, title, createdAt);
    }

    private Post savePost(Long authorId, String title, Instant createdAt) {
        return postRepository.save(Post.create(authorId, title, null, UUID.randomUUID() + ".png", createdAt));
    }

    private void saveCounts(Long postId, int hearts, int views) {
        PostHeartCount heartCount = PostHeartCount.create(postId);
        for (int index = 0; index < hearts; index++) {
            heartCount.increment();
        }
        postHeartCountRepository.save(heartCount);

        PostViewCount viewCount = PostViewCount.create(postId);
        for (int index = 0; index < views; index++) {
            viewCount.increment();
        }
        postViewCountRepository.save(viewCount);
    }
}
