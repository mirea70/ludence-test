package com.test.ludence.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.ludence.auth.security.JwtAuthenticationFilter;
import com.test.ludence.auth.security.RestAuthenticationEntryPoint;
import com.test.ludence.auth.service.AuthService;
import com.test.ludence.auth.service.AuthWithdrawalService;
import com.test.ludence.post.service.PostCreateService;
import com.test.ludence.post.service.PostImageService;
import com.test.ludence.post.service.PostQueryService;
import com.test.ludence.post.service.PostUpdateService;
import com.test.ludence.user.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
public abstract class ControllerTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected ObjectMapper objectMapper;

    @MockitoBean
    protected AuthService authService;

    @MockitoBean
    protected AuthWithdrawalService authWithdrawalService;

    @MockitoBean
    protected JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    protected RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected PostCreateService postCreateService;

    @MockitoBean
    protected PostImageService postImageService;

    @MockitoBean
    protected PostQueryService postQueryService;

    @MockitoBean
    protected PostUpdateService postUpdateService;
}
