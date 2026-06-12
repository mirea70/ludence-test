package com.test.ludence.search.domain.event;

public record PostSearchedEvent(Long userId, String query) {
}
