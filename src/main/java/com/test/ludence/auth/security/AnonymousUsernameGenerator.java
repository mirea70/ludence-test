package com.test.ludence.auth.security;

import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class AnonymousUsernameGenerator {

    private static final int RANDOM_LENGTH = 22;

    public String generate() {
        String randomValue = UUID.randomUUID().toString().replace("-", "");
        return "deleted_" + randomValue.substring(0, RANDOM_LENGTH);
    }
}
