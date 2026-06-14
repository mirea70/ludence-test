package com.test.ludens.common.storage;

import java.nio.file.Path;

public record StagedImage(String key, Path path) {
}
