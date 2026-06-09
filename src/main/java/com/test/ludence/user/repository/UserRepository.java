package com.test.ludence.user.repository;

import com.test.ludence.user.domain.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

    boolean existsByUsernameValue(String username);

    Optional<User> findByUsernameValueAndDeletedAtIsNull(String username);
}
