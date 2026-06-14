package com.test.ludence.scheduler;

import com.test.ludence.user.service.UserActivityCleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class UserActivityCleanupScheduler {

    private final UserActivityCleanupService cleanupService;

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanup() {
        cleanupService.cleanup();
    }
}
