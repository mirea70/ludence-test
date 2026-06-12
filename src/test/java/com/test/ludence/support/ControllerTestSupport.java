package com.test.ludence.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.ludence.auth.security.JwtAuthenticationFilter;
import com.test.ludence.auth.security.RestAuthenticationEntryPoint;
import com.test.ludence.auth.service.AuthService;
import com.test.ludence.auth.service.AuthWithdrawalService;
import com.test.ludence.heart.service.HeartCreateService;
import com.test.ludence.heart.service.HeartDeleteService;
import com.test.ludence.heart.service.HeartQueryService;
import com.test.ludence.post.service.PostCreateService;
import com.test.ludence.post.service.PostDeleteService;
import com.test.ludence.post.service.PostImageService;
import com.test.ludence.post.service.PostQueryService;
import com.test.ludence.post.service.PostUpdateService;
import com.test.ludence.recommendation.service.RecommendationQueryService;
import com.test.ludence.search.service.PostSearchService;
import com.test.ludence.user.service.UserService;
import com.test.ludence.user.service.UserHeartQueryService;
import com.test.ludence.user.service.UserPostQueryService;
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
    protected HeartCreateService heartCreateService;

    @MockitoBean
    protected HeartDeleteService heartDeleteService;

    @MockitoBean
    protected HeartQueryService heartQueryService;

    @MockitoBean
    protected JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    protected RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @MockitoBean
    protected UserService userService;

    @MockitoBean
    protected UserPostQueryService userPostQueryService;

    @MockitoBean
    protected UserHeartQueryService userHeartQueryService;

    @MockitoBean
    protected PostCreateService postCreateService;

    @MockitoBean
    protected PostDeleteService postDeleteService;

    @MockitoBean
    protected PostImageService postImageService;

    @MockitoBean
    protected PostQueryService postQueryService;

    @MockitoBean
    protected PostUpdateService postUpdateService;

    @MockitoBean
    protected PostSearchService postSearchService;

    @MockitoBean
    protected RecommendationQueryService recommendationQueryService;
}
