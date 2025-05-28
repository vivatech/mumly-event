package com.vivatech.mumly_event.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        // Bypass URIs starting with "/users"
        if (skipURL(request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String authorizationHeader = request.getHeader("Authorization");
        String token = null;
        String username = null;

        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            token = authorizationHeader.substring(7);
            username = jwtUtil.extractUsername(token);
        } else if (request.getQueryString() != null && request.getQueryString().contains("token=")) {
            token = request.getParameter("token");
        }

        if (username != null && jwtUtil.validateToken(token)) {
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(username, null, null);
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            // Set authentication to security context
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid or missing token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private boolean skipURL(String requestURI){
        List<String> skipURL = Arrays.asList("/api/v1/event/users/**", "/api/mpesaCallback", "/api/mpesaCallback/**",
                "/v3/api-docs/**",
                "/swagger-ui/**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/**",
                "/api-docs/**"
        );
        AntPathMatcher pathMatcher = new AntPathMatcher();

        return skipURL.stream().anyMatch(pattern -> pathMatcher.match(pattern, requestURI));
    }
}
