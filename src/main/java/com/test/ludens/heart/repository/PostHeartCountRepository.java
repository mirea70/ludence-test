package com.test.ludens.heart.repository;

import com.test.ludens.heart.domain.entity.PostHeartCount;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostHeartCountRepository extends JpaRepository<PostHeartCount, Long>, PostHeartCountRepositoryCustom {
}
