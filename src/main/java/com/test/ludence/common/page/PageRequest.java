package com.test.ludence.common.page;

import com.test.ludence.common.error.exception.DomainException;
import com.test.ludence.common.error.info.CommonErrorInfo;

public record PageRequest(int page, int limit) {

    public static final int DEFAULT_PAGE = 1;
    public static final int DEFAULT_LIMIT = 20;
    public static final int MAX_LIMIT = 100;

    public PageRequest {
        if (page < 1 || limit < 1 || limit > MAX_LIMIT) {
            throw new DomainException(CommonErrorInfo.INVALID_PAGE);
        }
    }

    public long offset() {
        return (long) (page - 1) * limit;
    }
}
