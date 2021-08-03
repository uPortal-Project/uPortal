package org.apereo.portal.health;

import static java.util.stream.Collectors.toMap;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
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

    @Autowired private List<? extends IHealthChecker> checkers;

    @RequestMapping(method = RequestMethod.GET, produces = "application/json")
    public Map<String, Map<String, Object>> healthCheck(
            @RequestParam(required = false) List<String> detail) throws RuntimeException {
        Map<String, Map<String, Object>> map = new HashMap<>();

        if (detail == null || detail.isEmpty()) {
            // Do nothing, just return HTTP 200, OK
            logger.debug("Doing a health check...");
        } else {
            if (detail.contains("ALL")) {
                map =
                        checkers.parallelStream()
                                .collect(toMap(IHealthChecker::getName, IHealthChecker::runCheck));
            } else {
                map =
                        checkers.parallelStream()
                                .filter(c -> detail.contains(c.getDetailIdentifier()))
                                .collect(toMap(IHealthChecker::getName, IHealthChecker::runCheck));
            }
            map.put("Time", getTime());
        }

        return map;
    }

    Map<String, Object> getTime() {
        Map<String, Object> map = new HashMap<>();
        long ms = System.currentTimeMillis();
        map.put("Current Time (ms)", Long.valueOf(ms));
        String time = DateTimeFormatter.RFC_1123_DATE_TIME.format(ZonedDateTime.now());
        map.put("Current Time", time);
        return map;
    }
}
