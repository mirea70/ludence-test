package com.test.ludens.post.service;

import com.test.ludens.post.repository.PostViewCountRepository;
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
        postViewCountRepository.increment(postId);
    }
}
