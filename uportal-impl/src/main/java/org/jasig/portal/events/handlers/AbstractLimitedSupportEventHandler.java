package org.jasig.portal.events.handlers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.events.EventHandler;
import org.jasig.portal.events.PortalEvent;
import org.springframework.beans.factory.InitializingBean;

/**
 * Abstract implemenation that allows for one EventHandler to handle many events
 * configurably without being defined multiple times.
 * <p>
 * Takes an array of Class objects and compares them to the current event by
 * doing an isAssignableFrom check (rather than a strict equals comparison).
 * 
 * @author Scott Battaglia
 * @version $Revision$ $Date$
 * @since 2.6
 * 
 */
public abstract class AbstractLimitedSupportEventHandler implements
		EventHandler, InitializingBean {

	/** Protected logging instance. */
	protected Log log = LogFactory.getLog(this.getClass());

	/** The default supported class of PortalEvent. */
	private final Class[] DEFAULT_SUPPORTED_EVENTS = new Class[] { PortalEvent.class };

	/** The list of supported classes. */
	private Class[] supportedEvents;

	public final boolean supports(final PortalEvent event) {
		for (int i = 0; i < supportedEvents.length; i++) {
			final Class supportedEvent = supportedEvents[i];
			if (supportedEvent.isAssignableFrom(event.getClass())) {
				return true;
			}
		}
		return false;
	}

	public final void afterPropertiesSet() throws Exception {
		if (supportedEvents == null) {
			log
					.info("No supported events set.   Using default of all PortalEvents.");
			this.supportedEvents = DEFAULT_SUPPORTED_EVENTS;
		}

		afterPropertiesSetInternal();
	}

	protected void afterPropertiesSetInternal() throws Exception {
	}

	public void setSupportedEvents(final Class[] supportedEvents) {
		this.supportedEvents = supportedEvents;
	}

}
