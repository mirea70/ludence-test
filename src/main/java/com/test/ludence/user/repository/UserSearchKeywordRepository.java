package com.test.ludence.user.repository;

import com.test.ludence.user.domain.entity.UserSearchKeyword;
import com.test.ludence.user.domain.vo.UserSearchKeywordId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSearchKeywordRepository
        extends JpaRepository<UserSearchKeyword, UserSearchKeywordId>, UserSearchKeywordRepositoryCustom {
}
