package com.test.ludence.user.service.listener;

import com.test.ludence.post.domain.event.PostViewedEvent;
import com.test.ludence.search.domain.event.PostSearchedEvent;
import com.test.ludence.user.service.UserActivityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class UserActivityEventListener {

    private final UserActivityService userActivityService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("userActivityTaskExecutor")
    public void handle(PostViewedEvent event) {
        if (event.userId() == null) {
            return;
        }
        userActivityService.recordPostView(event.postId(), event.userId());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Async("userActivityTaskExecutor")
    public void handle(PostSearchedEvent event) {
        userActivityService.recordSearch(event.userId(), event.query());
    }

}
