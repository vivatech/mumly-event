package com.vivatech.mumly_event.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Getter;

public class IntaSendAPIExceptionHandler extends RuntimeException {

    @Getter
    private final int code;
    private final String message;

    public IntaSendAPIExceptionHandler(String jsonBody) {
        int code = 0;
        String message = "";
        try {
            var json = new ObjectMapper().readTree(jsonBody);
            System.out.println("Received jsonBody " + jsonBody);
            code = 400;
            message = json.path("detail").asText();
            System.out.println("Parsed Json response: " + code + " " + message);
        } catch (Exception e) {
            throw new CustomExceptionHandler("Conversion Failed.");
        }
        this.code = code;
        this.message = message;
    }

    @Override public String getMessage() { return message; }
}
