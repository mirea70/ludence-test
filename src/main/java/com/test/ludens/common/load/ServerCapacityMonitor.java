package com.test.ludens.common.load;

import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import java.util.concurrent.ThreadPoolExecutor;
import javax.sql.DataSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.boot.web.embedded.tomcat.TomcatWebServer;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;

@Component
@RequiredArgsConstructor
public class ServerCapacityMonitor {

    private final WebApplicationContext webApplicationContext;
    private final DataSource dataSource;

    private volatile ThreadPoolExecutor tomcatExecutor;

    public boolean isSaturated() {
        return isTomcatSaturated(resolveTomcatExecutor()) || isDatabaseSaturated(resolveHikariPool());
    }

    static boolean isTomcatSaturated(ThreadPoolExecutor executor) {
        return executor != null && executor.getActiveCount() >= executor.getMaximumPoolSize();
    }

    static boolean isDatabaseSaturated(HikariPoolMXBean pool) {
        return pool != null && pool.getIdleConnections() == 0 && pool.getThreadsAwaitingConnection() > 0;
    }

    private ThreadPoolExecutor resolveTomcatExecutor() {
        if (tomcatExecutor != null) {
            return tomcatExecutor;
        }
        if (!(webApplicationContext instanceof WebServerApplicationContext serverApplicationContext)) {
            return null;
        }
        if (!(serverApplicationContext.getWebServer() instanceof TomcatWebServer tomcatWebServer)) {
            return null;
        }

        var executor = tomcatWebServer.getTomcat().getConnector().getProtocolHandler().getExecutor();
        if (executor instanceof ThreadPoolExecutor threadPoolExecutor) {
            tomcatExecutor = threadPoolExecutor;
        }
        return tomcatExecutor;
    }

    private HikariPoolMXBean resolveHikariPool() {
        if (!(dataSource instanceof HikariDataSource hikariDataSource)) {
            return null;
        }
        return hikariDataSource.getHikariPoolMXBean();
    }
}
