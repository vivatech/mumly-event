package com.vivatech.mumly_event.logging;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Collections;
import java.util.stream.Collectors;

public class HeadersUtil {
    public static String getHeadersAsString(HttpServletRequest request) {
        return Collections.list(request.getHeaderNames())
                .stream()
                .map(headerName -> headerName + ": " + Collections.list(request.getHeaders(headerName)))
                .collect(Collectors.joining(", "));
    }

    public static String getHeadersAsString(HttpServletResponse response) {
        return response.getHeaderNames()
                .stream()
                .map(headerName -> headerName + ": " + response.getHeaders(headerName))
                .collect(Collectors.joining(", "));
    }
}
