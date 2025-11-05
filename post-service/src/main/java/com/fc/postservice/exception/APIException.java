package com.fc.postservice.exception;


import java.io.Serial;

public class APIException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    int code;

    public APIException(String message, int code) {
        super(message);
        this.code=code;
    }
}
