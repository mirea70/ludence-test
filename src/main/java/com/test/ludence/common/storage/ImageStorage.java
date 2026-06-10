package com.test.ludence.common.storage;

import java.io.InputStream;

public interface ImageStorage {

    StagedImage stage(InputStream inputStream);

    void commit(StagedImage stagedImage);

    void discard(StagedImage stagedImage);

    void delete(String imageKey);
}
