package com.test.ludence.common.storage;

import java.nio.file.Path;

public record StagedImage(String key, Path path) {
}
