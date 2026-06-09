package com.test.ludence.auth.security;

public interface PasswordHasher {

    String hash(String rawPassword);

    boolean matches(String rawPassword, String encodedPassword);
}
