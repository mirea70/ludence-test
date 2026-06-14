package com.test.ludens.user.repository;

import com.test.ludens.user.domain.entity.UserSearchKeyword;
import com.test.ludens.user.domain.vo.UserSearchKeywordId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserSearchKeywordRepository
        extends JpaRepository<UserSearchKeyword, UserSearchKeywordId>, UserSearchKeywordRepositoryCustom {
}
