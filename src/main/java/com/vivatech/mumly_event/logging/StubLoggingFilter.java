package com.vivatech.mumly_event.logging;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Component
public class StubLoggingFilter implements Filter {
    private static final Logger log = LoggerFactory.getLogger(StubLoggingFilter.class);

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper((HttpServletRequest) request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper((HttpServletResponse) response);

        if (requestWrapper.getRequestURI().startsWith("/swagger-ui")
                || requestWrapper.getRequestURI().startsWith("/v3/api-docs")
                || requestWrapper.getRequestURI().startsWith("/api-docs")
                || requestWrapper.getRequestURI().startsWith("/swagger-resources")) {
            chain.doFilter(request, response);
            return;
        }
        long startTime = System.currentTimeMillis();
        try {
            chain.doFilter(requestWrapper, responseWrapper);
        } finally {
            logRequest(requestWrapper);
            logResponse(responseWrapper, System.currentTimeMillis() - startTime);
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String requestBody = new String(request.getContentAsByteArray(), StandardCharsets.UTF_8);
        String queryParams = request.getQueryString() != null ? "?" + request.getQueryString() : "";
        log.info("\n============================= Request Begin ===============================\n" +
                        "URI         : {}\n" +
                        "QueryParams : {}\n" +
                        "Method      : {}\n" +
                        "Request body: {}\n",
                request.getRequestURI(),
                queryParams,
                request.getMethod(),
                requestBody);
    }

    private void logResponse(ContentCachingResponseWrapper response, long timeElapsed) {
        String responseBody = new String(response.getContentAsByteArray(), StandardCharsets.UTF_8);
        log.info("\n" +
                        "Status code  : {}\n" +
                        "Response body: {}\n" +
                        "Time elapsed : {} ms\n" +
                        "============================= Response End ================================",
                response.getStatus(),
                responseBody,
                timeElapsed);
    }
}
