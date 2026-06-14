package com.test.ludens.post.repository;

import com.test.ludens.post.domain.entity.PostViewCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostViewCountRepository
        extends JpaRepository<PostViewCount, Long>, PostViewCountRepositoryCustom {
}
