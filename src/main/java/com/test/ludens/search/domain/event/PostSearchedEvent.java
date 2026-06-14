package com.test.ludens.search.domain.event;

public record PostSearchedEvent(Long userId, String query) {
}
