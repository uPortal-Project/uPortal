package org.jasig.portal.aggr;

import org.jasig.portal.portlet.om.IPortletDefinition;

public interface PortletRatingAggregator {
    /**
     * This function aggregates information collected in
     * {@link IMarketplaceRating} into {@link IPortletDefinition}. 
     * This does utilize a cluster lock
     * @return true if successful, false if failure
     */
    boolean aggregatePortletRatings();
}
