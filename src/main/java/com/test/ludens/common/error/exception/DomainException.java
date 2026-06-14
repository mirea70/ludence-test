package com.test.ludens.common.error.exception;

import com.test.ludens.common.error.info.ErrorInfo;

public class DomainException extends RuntimeException {

    private final ErrorInfo errorInfo;

    public DomainException(ErrorInfo errorInfo) {
        super(errorInfo.getMessage());
        this.errorInfo = errorInfo;
    }

    public ErrorInfo getErrorInfo() {
        return errorInfo;
    }
}
