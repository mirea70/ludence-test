package com.test.ludence.common.storage;

import java.io.InputStream;
import org.springframework.core.io.Resource;

public interface ImageStorage {

    StagedImage stage(InputStream inputStream);

    void commit(StagedImage stagedImage);

    void discard(StagedImage stagedImage);

    Resource get(String imageKey);

    void delete(String imageKey);
}
