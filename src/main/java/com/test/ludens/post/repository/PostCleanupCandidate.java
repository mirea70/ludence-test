package com.test.ludens.post.repository;

import java.time.Instant;

public record PostCleanupCandidate(Long postId, String imageKey, Instant deletedAt) {
}
