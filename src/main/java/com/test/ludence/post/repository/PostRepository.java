package com.test.ludence.post.repository;

import com.test.ludence.post.domain.entity.Post;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PostRepository extends JpaRepository<Post, Long>, PostRepositoryCustom {

    boolean existsByIdAndDeletedAtIsNull(Long id);

    Optional<Post> findByIdAndDeletedAtIsNull(Long id);
}
