package com.test.ludens.heart.repository;

import com.test.ludens.heart.domain.entity.Heart;
import com.test.ludens.heart.domain.vo.HeartId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HeartRepository extends JpaRepository<Heart, HeartId>, HeartRepositoryCustom {
}
