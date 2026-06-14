package com.test.ludens.common.load;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import com.zaxxer.hikari.HikariPoolMXBean;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

@DisplayName("ServerCapacityMonitor 테스트")
class ServerCapacityMonitorTest {

    @Test
    @DisplayName("Tomcat 활성 스레드가 최대 스레드 수에 도달하면 포화 상태다")
    void detectsTomcatSaturation_whenAllThreadsAreActive() {
        // given
        ThreadPoolExecutor executor = Mockito.mock(ThreadPoolExecutor.class);
        given(executor.getActiveCount()).willReturn(200);
        given(executor.getMaximumPoolSize()).willReturn(200);

        // when & then
        assertThat(ServerCapacityMonitor.isTomcatSaturated(executor)).isTrue();
    }

    @Test
    @DisplayName("Tomcat에 사용 가능한 스레드가 있으면 포화 상태가 아니다")
    void doesNotDetectTomcatSaturation_whenThreadIsAvailable() {
        // given
        ThreadPoolExecutor executor = new ThreadPoolExecutor(
                1,
                2,
                0,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<>()
        );

        // when & then
        assertThat(ServerCapacityMonitor.isTomcatSaturated(executor)).isFalse();
        executor.shutdown();
    }

    @Test
    @DisplayName("Hikari 유휴 커넥션이 없고 커넥션 대기 스레드가 존재하면 포화 상태다")
    void detectsDatabaseSaturation_whenConnectionsAreExhaustedAndThreadIsWaiting() {
        // given
        HikariPoolMXBean pool = Mockito.mock(HikariPoolMXBean.class);
        given(pool.getIdleConnections()).willReturn(0);
        given(pool.getThreadsAwaitingConnection()).willReturn(1);

        // when & then
        assertThat(ServerCapacityMonitor.isDatabaseSaturated(pool)).isTrue();
    }

    @Test
    @DisplayName("Hikari 유휴 커넥션이 없더라도 대기 스레드가 없으면 포화 상태가 아니다")
    void doesNotDetectDatabaseSaturation_whenNoThreadIsWaiting() {
        // given
        HikariPoolMXBean pool = Mockito.mock(HikariPoolMXBean.class);
        given(pool.getIdleConnections()).willReturn(0);
        given(pool.getThreadsAwaitingConnection()).willReturn(0);

        // when & then
        assertThat(ServerCapacityMonitor.isDatabaseSaturated(pool)).isFalse();
    }
}
