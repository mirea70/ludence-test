package com.test.ludens.common.config;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RequiredArgsConstructor
public class AsyncTaskRejectionHandler implements RejectedExecutionHandler {

    private final String executorName;

    @Override
    public void rejectedExecution(Runnable task, ThreadPoolExecutor executor) {
        log.warn(
                "Discarded async task because executor is saturated: executor={}, active={}, poolSize={}, queued={}",
                executorName,
                executor.getActiveCount(),
                executor.getPoolSize(),
                executor.getQueue().size()
        );
    }
}
