package com.wateracademy.exception;

import org.springframework.http.HttpStatusCode;

public class ExternalServiceException extends RuntimeException {

    private final HttpStatusCode statusCode;

    public ExternalServiceException(HttpStatusCode statusCode, String message) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpStatusCode getStatusCode() {
        return statusCode;
    }
}
