package org.apereo.portal.aggr.analytics;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apereo.portal.events.AnalyticsPortalEvent;
import org.apereo.portal.events.LoginEvent;
import org.apereo.portal.events.aggr.analytics.AnalyticsEventAggregator;
import org.junit.Test;

public class AnalyticsEventAggregatorTests {

    @Test
    public void testSupports() {
        AnalyticsEventAggregator aggr = new AnalyticsEventAggregator();
        assertTrue(aggr.supports(AnalyticsPortalEvent.class));
        assertFalse(aggr.supports(LoginEvent.class));
    }
}
