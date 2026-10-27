package com.techflow.common.exception;

import lombok.Getter;

@Getter
public class UnauthorizedAccessException extends RuntimeException {

    private final String errorCode;

    public UnauthorizedAccessException(String message) {
        super(message);
        this.errorCode = "UNAUTHORIZED";
    }

    public UnauthorizedAccessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
}