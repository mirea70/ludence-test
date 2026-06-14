package com.test.ludens.common.error.exception;

import com.test.ludens.common.error.info.ErrorInfo;

public class BusinessException extends RuntimeException {

    private final ErrorInfo errorInfo;

    public BusinessException(ErrorInfo errorInfo) {
        super(errorInfo.getMessage());
        this.errorInfo = errorInfo;
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }
}
