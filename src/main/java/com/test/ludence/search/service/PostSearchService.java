package com.test.ludence.search.service;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.common.page.PageRequest;
import com.test.ludence.post.dto.response.PostDetailResponse;
import com.test.ludence.post.dto.response.PostPageResponse;
import com.test.ludence.post.repository.PostRepository;
import com.test.ludence.search.domain.info.SearchErrorInfo;
import com.test.ludence.search.domain.event.PostSearchedEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostSearchService {

    private static final int MAX_QUERY_LENGTH = 100;

    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PostPageResponse searchPosts(String query, int page, int limit, Long currentUserId) {
        PageRequest pageRequest = new PageRequest(page, limit);
        String normalizedQuery = normalizeQuery(query);

        List<PostDetailResponse> posts = postRepository.findActiveDetailsByQuery(
                normalizedQuery,
                currentUserId,
                pageRequest
        );
        long total = postRepository.countActiveByQuery(normalizedQuery);
        if (currentUserId != null && normalizedQuery != null) {
            eventPublisher.publishEvent(new PostSearchedEvent(currentUserId, normalizedQuery));
        }
        return new PostPageResponse(page, limit, total, posts);
    }

    private String normalizeQuery(String query) {
        if (query == null) {
            return null;
        }
        if (query.length() > MAX_QUERY_LENGTH) {
            throw new DomainException(SearchErrorInfo.INVALID_QUERY);
        }
        if (query.isBlank()) {
            return null;
        }
        return query;
    }
}
