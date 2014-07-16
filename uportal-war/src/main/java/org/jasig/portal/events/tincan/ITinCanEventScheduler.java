package org.jasig.portal.events.tincan;

import java.util.List;

import org.jasig.portal.events.tincan.om.LrsStatement;
import org.jasig.portal.events.tincan.providers.ITinCanAPIProvider;


/**
 * API that controls when API events are sent off to a provider.
 * 
 * @author Josh Helmer, jhelmer@unicon.net
 */
public interface ITinCanEventScheduler {
    /**
     * Set the list of xAPI providers to process each event.
     *
     * @param providers the list of providers
     */
    void setProviders(List<ITinCanAPIProvider> providers);


    /**
     * Schedule an event for processing.
     *
     * @param statement the statement to schedule.
     */
    void scheduleEvent(LrsStatement statement);
}
