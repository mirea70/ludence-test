package com.test.ludence.post.service;

import com.test.ludence.post.domain.entity.PostViewCount;
import com.test.ludence.post.repository.PostViewCountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostViewCountService {

    private final PostViewCountRepository postViewCountRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordView(Long postId) {
        PostViewCount viewCount = postViewCountRepository.findByIdForUpdate(postId)
                .orElseGet(() -> postViewCountRepository.save(PostViewCount.create(postId)));
        viewCount.increment();
    }
}
