package org.apereo.portal.events.aggr.analytics;

import org.apereo.portal.events.AnalyticsPortalEvent;
import org.apereo.portal.events.PortalEvent;
import org.apereo.portal.events.aggr.BasePortalEventAggregator;
import org.apereo.portal.events.aggr.SimplePortalEventAggregator;
import org.apereo.portal.events.aggr.session.EventSession;
import org.apereo.portal.events.handlers.db.IPortalEventDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class AnalyticsEventAggregator extends BasePortalEventAggregator<PortalEvent>
        implements SimplePortalEventAggregator<PortalEvent> {

    @Value("${org.apereo.portal.events.aggr.analytics.AnalyticsEventAggregator.status:DISABLED}")
    private String status;

    @Autowired private IPortalEventDao store;

    @Override
    public boolean supports(Class<? extends PortalEvent> type) {
        return AnalyticsPortalEvent.class.isAssignableFrom(type);
    }

    @Override
    public void aggregateEvent(PortalEvent e, EventSession eventSession) {
        if ("ENABLED".equalsIgnoreCase(status)) {
            store.storeAnalyticsEvent(e);
        }
    }
}
