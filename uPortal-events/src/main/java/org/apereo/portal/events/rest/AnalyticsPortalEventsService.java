package org.apereo.portal.events.rest;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apereo.portal.events.IPortalAnalyticsEventFactory;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AnalyticsPortalEventsService {
	protected final Logger logger = LoggerFactory.getLogger(getClass());

	@Autowired
	private IPortalAnalyticsEventFactory portalEventFactory;
	@Autowired
	private IPersonManager personManager;

	public void publishEvent(HttpServletRequest request, Map<String, Object> analyticsData) {
		final IPerson user = personManager.getPerson(request);
		portalEventFactory.publishAnalyticsPortalEvents(request, this, analyticsData, user);
	}
}
