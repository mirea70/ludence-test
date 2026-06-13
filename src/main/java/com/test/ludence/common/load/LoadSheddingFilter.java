package com.test.ludence.common.load;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.ludence.common.error.info.SystemErrorInfo;
import com.test.ludence.common.error.response.ErrorResponse;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class LoadSheddingFilter extends OncePerRequestFilter {

    private static final String HEALTH_CHECK_PATH = "/debug/health";
    private static final String RETRY_AFTER_SECONDS = "1";

    private final ServerCapacityMonitor capacityMonitor;
    private final ObjectMapper objectMapper;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return HEALTH_CHECK_PATH.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        if (!capacityMonitor.isSaturated()) {
            filterChain.doFilter(request, response);
            return;
        }

        SystemErrorInfo errorInfo = SystemErrorInfo.CAPACITY_EXCEEDED;
        ErrorResponse errorResponse = new ErrorResponse(
                errorInfo.getCode(),
                errorInfo.getMessage(),
                request.getRequestURI()
        );
        response.setStatus(errorInfo.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.setHeader("Retry-After", RETRY_AFTER_SECONDS);
        objectMapper.writeValue(response.getWriter(), errorResponse);
    }
}
