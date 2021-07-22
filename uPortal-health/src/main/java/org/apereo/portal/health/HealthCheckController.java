package org.apereo.portal.health;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * Health Check that can include some additional details
 *
 * <p>- Database response time - Total Memory {@code Runtime.getRuntime().totalMemory()} - Max
 * Memory {@code Runtime.getRuntime().maxMemory()} - Free Memory {@code
 * Runtime.getRuntime().freeMemory()} - Current Time - {@code System.currentTimeMillis()}
 */
@RestController
public class HealthCheckController {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Autowired
    @Qualifier("PortalDb")
    private TomcatDataSourceFactory dataSourceFactory;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public Map<String, Long> healthCheck(@RequestParam(required = false) List<String> detail)
            throws DbQueryTimeoutException {
        Map<String, Long> map = new HashMap<>();

        if (detail == null && detail.isEmpty()) {
            // Do nothing, just return HTTP 200, OK
            logger.debug("Doing a health check...");
        } else {
            map.put("Current Time (ms)", System.currentTimeMillis());
            if (detail.contains("MEMORY")) {
                map.put("Total Memory", Runtime.getRuntime().totalMemory());
                map.put("Max Memory", Runtime.getRuntime().maxMemory());
                map.put("Free Memory", Runtime.getRuntime().freeMemory());
            }
            if (detail.contains("DB")) {
                map.put("Database Response Time (ns)", getDbResponseTime());
            }
        }

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
                                long startTime = System.currentTimeMillis();
                                startTime = System.nanoTime();
                                statement.execute(query);
                                long endTime = System.currentTimeMillis();
                                endTime = System.nanoTime();
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
