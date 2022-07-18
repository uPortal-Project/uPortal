package org.apereo.portal.health;

import com.sun.istack.NotNull;
import java.util.Map;

/**
 * Interface for health checker that performs a test and either returns some useful information that
 * will be added to a JSON response OR throws a {@code RuntimeException} if the health check should
 * respond with a code 500. This will likely cause a loadbalancer or other service report that
 * uPortal server is failing.
 */
public interface IHealthChecker {

    /* Human readable name for an implementation */
    @NotNull
    String getName();

    /* String-based token passed to health checker in details parameter */
    @NotNull
    String getDetailIdentifier();

    /* Actual check code */
    @NotNull
    Map<String, Object> runCheck() throws RuntimeException;
}
