package com.fc.postservice.exception;


import lombok.Getter;

import java.io.Serial;

@Getter
public class APIException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 1L;

    private final int code;

    public APIException() {
        super();
        this.code = 0;
    }
    public APIException(String message) {
        super(message);
        this.code = 0;
    }

    public APIException(String message, int code) {
        super(message);
        this.code=code;
    }

}
