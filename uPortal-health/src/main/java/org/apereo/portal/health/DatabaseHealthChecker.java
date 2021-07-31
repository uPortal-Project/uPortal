package org.apereo.portal.health;

import com.sun.istack.NotNull;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.apache.tomcat.jdbc.pool.DataSource;
import org.jasig.portlet.utils.jdbc.TomcatDataSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.ResponseStatus;

@Component
public class DatabaseHealthChecker implements IHealthChecker {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("PortalDb")
    private TomcatDataSourceFactory dataSourceFactory;

    @Override
    @NotNull
    public String getName() {
        return "Database Health Checker";
    }

    @Override
    @NotNull
    public String getDetailIdentifier() {
        return "DB";
    }

    @Override
    @NotNull
    public Map<String, Object> runCheck() throws RuntimeException {
        Map<String, Object> map = new HashMap<>();
        Long ns = getDbResponseTime();
        map.put("detail", getDetailIdentifier());
        map.put("Database Response Time (ns)", ns);
        map.put("Database Response Time (ms)", ns / 1000000L);
        return map;
    }

    Long getDbResponseTime() throws DbQueryFailedException {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Future<Long> future =
                executor.submit(
                        () -> {
                            final String query = dataSourceFactory.getValidationQuery();
                            DataSource dataSource = dataSourceFactory.getObject();
                            try (Connection connection = dataSource.getConnection()) {
                                Statement statement = connection.createStatement();
                                long startTime = System.nanoTime();
                                statement.execute(query);
                                long endTime = System.nanoTime();
                                return endTime - startTime;
                            } catch (SQLException e) {
                                logger.warn("Timed DB Query failed", e);
                                throw new DbQueryFailedException("Timed DB Query failed", e);
                            }
                        });

        try {
            return future.get(5, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            logger.warn("Timed DB Query timed out", e);
            throw new DbQueryTimeoutException("Timed DB Query timed out", e);
        } catch (Exception e) {
            logger.warn("Timed DB Query failed", e);
            throw new DbQueryFailedException("Timed DB Query failed", e);
        }
    }

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Database Query failed")
    public static class DbQueryFailedException extends RuntimeException {
        public DbQueryFailedException(String timed_db_query_failed, Exception e) {
            super(timed_db_query_failed, e);
        }
    }

    @ResponseStatus(code = HttpStatus.INTERNAL_SERVER_ERROR, reason = "Database Query timeout")
    public static class DbQueryTimeoutException extends RuntimeException {
        public DbQueryTimeoutException(String timed_db_query_failed, Exception e) {
            super(timed_db_query_failed, e);
        }
    }
}
