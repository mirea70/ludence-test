package com.test.ludence.user.repository;

import com.test.ludence.user.domain.entity.UserPostView;
import com.test.ludence.user.domain.vo.UserPostViewId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPostViewRepository
        extends JpaRepository<UserPostView, UserPostViewId>, UserPostViewRepositoryCustom {
}
