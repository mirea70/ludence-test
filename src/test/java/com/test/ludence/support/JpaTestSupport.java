package com.test.ludence.support;

import com.test.ludence.common.config.JpaConfig;
import com.test.ludence.common.config.DatabaseProduct;
import com.test.ludence.heart.repository.HeartRepository;
import com.test.ludence.heart.repository.PostHeartCountRepository;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.post.repository.PostViewCountRepository;
import com.test.ludence.recommendation.repository.RecommendationStateRepository;
import com.test.ludence.recommendation.repository.CommonRecommendationRepository;
import com.test.ludence.recommendation.repository.RecommendationCandidateQueryRepository;
import com.test.ludence.recommendation.repository.RecommendationQueryRepository;
import com.test.ludence.recommendation.repository.UserRecommendationRepository;
import com.test.ludence.user.repository.UserRepository;
import com.test.ludence.user.repository.UserPostViewRepository;
import com.test.ludence.user.repository.UserSearchKeywordRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import({
        JpaConfig.class,
        DatabaseProduct.class,
        RecommendationCandidateQueryRepository.class,
        RecommendationQueryRepository.class
})
public abstract class JpaTestSupport {

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected PostRepository postRepository;

    @Autowired
    protected HeartRepository heartRepository;

    @Autowired
    protected PostHeartCountRepository postHeartCountRepository;

    @Autowired
    protected PostViewCountRepository postViewCountRepository;

    @Autowired
    protected UserPostViewRepository userPostViewRepository;

    @Autowired
    protected UserSearchKeywordRepository userSearchKeywordRepository;

    @Autowired
    protected RecommendationStateRepository recommendationStateRepository;

    @Autowired
    protected CommonRecommendationRepository commonRecommendationRepository;

    @Autowired
    protected UserRecommendationRepository userRecommendationRepository;

    @Autowired
    protected RecommendationCandidateQueryRepository recommendationCandidateQueryRepository;

    @Autowired
    protected RecommendationQueryRepository recommendationQueryRepository;
}
