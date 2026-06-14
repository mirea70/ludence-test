package com.test.ludens.user.repository;

import com.test.ludens.user.domain.entity.UserPostView;
import com.test.ludens.user.domain.vo.UserPostViewId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserPostViewRepository
        extends JpaRepository<UserPostView, UserPostViewId>, UserPostViewRepositoryCustom {
}
