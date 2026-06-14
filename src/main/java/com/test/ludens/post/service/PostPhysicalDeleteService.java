package com.test.ludens.post.service;

import com.test.ludens.post.repository.PostRepository;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PostPhysicalDeleteService {

    private final PostRepository postRepository;

    @Transactional
    public void deleteExpiredPost(Long postId, Instant expiredAt) {
        postRepository.deleteExpiredPostData(postId, expiredAt);
    }
}
