package com.test.ludence.post.repository;

import com.test.ludence.post.domain.entity.PostViewCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostViewCountRepository
        extends JpaRepository<PostViewCount, Long>, PostViewCountRepositoryCustom {
}
