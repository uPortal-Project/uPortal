package org.jasig.portal.events;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;

import javax.portlet.PortletRequest;
import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

@Component
public class RequestScopedEventsTrackerImpl implements ApplicationListener<PortalEvent>, RequestScopedEventsTracker {
    private static final String REQUEST_EVENTS = RequestScopedEventsTrackerImpl.class.getName() + ".REQUEST_EVENTS";
    
    private IPortalRequestUtils portalRequestUtils;

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Override
    public void onApplicationEvent(PortalEvent event) {
        final HttpServletRequest portalRequest = event.getPortalRequest();
        if (portalRequest != null) {
            final ConcurrentMap<PortalEvent, Boolean> renderEvents = PortalWebUtils.getMapRequestAttribute(portalRequest, REQUEST_EVENTS);
            renderEvents.put(event, true);
        }
    }
    
    @Override
    public Set<PortalEvent> getRequestEvents(PortletRequest portletRequest) {
        final HttpServletRequest portletHttpRequest = this.portalRequestUtils.getPortletHttpRequest(portletRequest);
        return this.getRequestEvents(portletHttpRequest);
    }
    
    @Override
    public Set<PortalEvent> getRequestEvents(HttpServletRequest request) {
        request = this.portalRequestUtils.getOriginalPortalRequest(request);
        
        final ConcurrentMap<PortalEvent, Boolean> renderEvents = PortalWebUtils.getMapRequestAttribute(request, REQUEST_EVENTS, false);
        if (renderEvents == null) {
            return Collections.emptySet();
        }
        
        return Collections.unmodifiableSet(renderEvents.keySet());
    }
}
