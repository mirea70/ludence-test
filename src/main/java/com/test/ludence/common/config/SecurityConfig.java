package com.test.ludence.common.config;

import com.test.ludence.auth.security.JwtAuthenticationFilter;
import com.test.ludence.auth.security.RestAuthenticationEntryPoint;
import com.test.ludence.common.load.LoadSheddingFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.SecurityContextHolderFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final LoadSheddingFilter loadSheddingFilter;
    private final RestAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exception -> exception.authenticationEntryPoint(authenticationEntryPoint))
                .authorizeHttpRequests(request -> request
                        .requestMatchers("/auth/me").authenticated()
                        .requestMatchers(HttpMethod.POST, "/posts").authenticated()
                        .requestMatchers(HttpMethod.POST, "/posts/*/heart").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/posts/*/heart").authenticated()
                        .requestMatchers(HttpMethod.GET, "/posts/*/hearts").authenticated()
                        .requestMatchers(HttpMethod.GET, "/users/*/hearts").authenticated()
                        .requestMatchers(HttpMethod.PATCH, "/posts/*").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/posts/*").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(loadSheddingFilter, SecurityContextHolderFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public FilterRegistrationBean<LoadSheddingFilter> disableLoadSheddingFilterAutoRegistration() {
        FilterRegistrationBean<LoadSheddingFilter> registration = new FilterRegistrationBean<>(loadSheddingFilter);
        registration.setEnabled(false);
        return registration;
    }
}
