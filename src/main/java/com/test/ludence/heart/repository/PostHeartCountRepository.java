package com.test.ludence.heart.repository;

import com.test.ludence.heart.domain.entity.PostHeartCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHeartCountRepository extends JpaRepository<PostHeartCount, Long>, PostHeartCountRepositoryCustom {
}
