package com.test.ludence.heart.repository;

import com.test.ludence.heart.domain.entity.Heart;
import com.test.ludence.heart.domain.vo.HeartId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeartRepository extends JpaRepository<Heart, HeartId>, HeartRepositoryCustom {
}
