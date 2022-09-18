package org.apereo.portal.events;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apereo.portal.security.IPerson;

public interface IPortalAnalyticsEventFactory {

	void publishAnalyticsPortalEvents(HttpServletRequest request, Object source, Map<String, Object> analyticsData, IPerson person);
}
