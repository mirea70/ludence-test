package com.test.ludence.post.service.listener;

import com.test.ludence.post.domain.event.PostViewedEvent;
import com.test.ludence.post.service.PostViewCountService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class PostViewCountEventListener {

    private final PostViewCountService postViewCountService;

    @Async("postViewCountTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(PostViewedEvent event) {
        postViewCountService.recordView(event.postId());
    }
}
