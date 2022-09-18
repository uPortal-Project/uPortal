package org.apereo.portal.events;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.apereo.portal.security.IPerson;

public class AnalyticsPortalEvent extends PortalEvent {
	private static final long serialVersionUID = 1L;

	private final String user;
	private final Date eventDate;
	private final String type;
	private final String url;

	AnalyticsPortalEvent() {
		super();
		user = "UNKNOWN";
		eventDate = Calendar.getInstance(TimeZone.getTimeZone("UTC")).getTime();
		type = "UNKNOWN";
		url = "";
	}

	AnalyticsPortalEvent(PortalEventBuilder portalEventBuilder, IPerson user, Date eventDate, String type, String url) {
		super(portalEventBuilder);
		this.user = user.getUserName();
		this.eventDate = eventDate;
		this.type = type;
		this.url = url;
	}

	public Date getEventDate() {
		return eventDate;
	}

	public String getType() {
		return type;
	}

	public String getUrl() {
		return url;
	}

	public String getUser() {
		return this.user;
	}

	@Override
	public String toString() {
		return super.toString() + ", eventDate=" + this.eventDate + ", type=" + type + ", url=" + url + ", user="
				+ user;
	}
}
