package org.jasig.portal.portlet.container.cache;

import java.io.Serializable;

/**
 * Defines a cached portlet results
 * 
 * @author Eric Dalquist
 * @version $Revision$
 * @param <T>
 */
public interface CachedPortletResultHolder<T extends Serializable> {
    /**
     * @return The portlet result
     */
    T getPortletResult();
    
    /**
     * @return The time-since-epoch timestamp when the cached data will expire
     */
    long getExpirationTime();
    
    /**
     * @return The ETag if set by the portlet
     */
    String getEtag();
    
    /**
     * @return The time the result was cached
     */
    long getTimeStored();
}
