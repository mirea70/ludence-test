package com.test.ludens.user.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.test.ludens.common.error.exception.DomainException;
import com.test.ludens.common.error.info.UserErrorInfo;
import com.test.ludens.user.domain.vo.Username;
import jakarta.persistence.*;

import java.time.Instant;
import lombok.Getter;

@Getter
@Entity
@Table(
        name = "users",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_users_username",
                        columnNames = {"username"}
                )
        }
)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private Username username;

    @Column(length = 100)
    private String password;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    private Instant deletedAt;

    protected User() {
    }

    private User(Username username, String password, Instant createdAt) {
        this.username = username;
        this.password = password;
        this.createdAt = createdAt;
    }

    public static User create(String username, String encodedPassword, Instant createdAt) {
        validateEncodedPassword(encodedPassword);
        validateTime(createdAt);
        return new User(new Username(username), encodedPassword, createdAt);
    }

    public String getUsername() {
        return username.value();
    }

    @JsonIgnore
    public boolean isActive() {
        return deletedAt == null;
    }

    public void withdraw(String anonymizedUsername, Instant deletedAt) {
        if (!isActive()) {
            throw new DomainException(UserErrorInfo.ALREADY_WITHDRAWN);
        }
        validateTime(deletedAt);
        username = new Username(anonymizedUsername);
        password = null;
        this.deletedAt = deletedAt;
    }

    private static void validateEncodedPassword(String encodedPassword) {
        if (encodedPassword == null || encodedPassword.isBlank()) {
            throw new DomainException(UserErrorInfo.INVALID_PASSWORD);
        }
    }

    private static void validateTime(Instant time) {
        if (time == null) {
            throw new IllegalArgumentException("시간은 null일 수 없습니다.");
        }
    }
}
