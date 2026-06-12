package com.test.ludence.post.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.test.ludence.post.domain.entity.PostViewCount;
import com.test.ludence.post.repository.PostViewCountRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@DisplayName("PostViewCountService 테스트")
@ExtendWith(MockitoExtension.class)
class PostViewCountServiceTest {

    @Mock
    private PostViewCountRepository postViewCountRepository;

    @Test
    @DisplayName("포스트 조회를 기록하면 조회 수를 증가시킨다")
    void incrementsViewCount_whenPostViewIsRecorded() {
        // given
        PostViewCount viewCount = PostViewCount.create(10L);
        given(postViewCountRepository.findByIdForUpdate(10L)).willReturn(Optional.of(viewCount));
        PostViewCountService service = new PostViewCountService(postViewCountRepository);

        // when
        service.recordView(10L);

        // then
        assertThat(viewCount.getCount()).isEqualTo(1L);
    }
}
