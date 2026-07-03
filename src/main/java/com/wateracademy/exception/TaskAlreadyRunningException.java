package com.wateracademy.exception;

public class TaskAlreadyRunningException extends RuntimeException {
    public TaskAlreadyRunningException(String message) {
        super(message);
    }
}
