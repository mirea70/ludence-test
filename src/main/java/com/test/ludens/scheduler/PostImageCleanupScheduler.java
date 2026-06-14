package com.test.ludens.scheduler;

import com.test.ludens.post.service.PostImageCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PostImageCleanupScheduler {

    private final PostImageCleanupService cleanupService;

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanup() {
        cleanupService.cleanup();
    }
}
