package com.test.ludens.auth.security.filter;

import com.test.ludens.auth.security.dto.AuthenticatedUser;
import com.test.ludens.auth.security.error.RestAuthenticationEntryPoint;
import com.test.ludens.auth.security.provider.JwtTokenProvider;
import com.test.ludens.common.error.info.AuthErrorInfo;
import com.test.ludens.common.error.exception.BusinessException;
import com.test.ludens.user.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtTokenProvider jwtTokenProvider;
    private final UserRepository userRepository;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization == null) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            authenticate(authorization);
            filterChain.doFilter(request, response);
        } catch (BusinessException exception) {
            SecurityContextHolder.clearContext();
            authenticationEntryPoint.commence(request, response, null);
        }
    }

    private void authenticate(String authorization) {
        if (!authorization.startsWith(BEARER_PREFIX)) {
            throw new BusinessException(AuthErrorInfo.INVALID_TOKEN);
        }

        String token = authorization.substring(BEARER_PREFIX.length());
        Long userId = jwtTokenProvider.getUserId(token);
        userRepository.findByIdAndDeletedAtIsNull(userId)
                .orElseThrow(() -> new BusinessException(AuthErrorInfo.INVALID_TOKEN));

        AuthenticatedUser principal = new AuthenticatedUser(userId);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(principal, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
