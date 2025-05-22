package com.vivatech.mumly_event.exception;

public class CustomExceptionHandler extends RuntimeException {

    public CustomExceptionHandler() {
        super("Not found"); // Provide a default error message
    }

    public CustomExceptionHandler(String message) {
        super(message); // Allow custom error messages
    }
}
