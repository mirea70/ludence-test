package com.test.ludence.support;

import com.test.ludence.common.config.JpaConfig;
import com.test.ludence.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
public abstract class JpaTestSupport {

    @Autowired
    protected EntityManager entityManager;

    @Autowired
    protected UserRepository userRepository;
}
