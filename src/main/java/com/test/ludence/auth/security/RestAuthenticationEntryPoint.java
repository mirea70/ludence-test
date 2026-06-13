package com.test.ludence.auth.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.ludence.common.error.info.AuthErrorInfo;
import com.test.ludence.common.error.response.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {
        AuthErrorInfo errorInfo = AuthErrorInfo.INVALID_TOKEN;
        response.setStatus(errorInfo.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(
                response.getOutputStream(),
                new ErrorResponse(clock, errorInfo.getCode(), errorInfo.getMessage(), request.getRequestURI())
        );
    }
}
