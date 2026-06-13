package com.test.ludence.support;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.test.ludence.auth.security.filter.JwtAuthenticationFilter;
import com.test.ludence.auth.security.error.RestAuthenticationEntryPoint;
import com.test.ludence.auth.service.AuthService;
import com.test.ludence.auth.service.AuthWithdrawalService;
import com.test.ludence.common.config.TimeConfig;
import com.test.ludence.common.load.LoadSheddingFilter;
import com.test.ludence.debug.service.DebugService;
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
import org.springframework.boot.autoconfigure.web.servlet.MultipartProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Import(TimeConfig.class)
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
    protected LoadSheddingFilter loadSheddingFilter;

    @MockitoBean
    protected RestAuthenticationEntryPoint restAuthenticationEntryPoint;

    @MockitoBean
    protected MultipartProperties multipartProperties;

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

    @MockitoBean
    protected DebugService debugService;
}
