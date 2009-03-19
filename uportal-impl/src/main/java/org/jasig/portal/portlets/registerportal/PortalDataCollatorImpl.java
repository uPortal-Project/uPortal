/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * Collates data from multiple {@link IPortalDataCollector}s
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalDataCollatorImpl implements IPortalDataCollator {
    private Map<String, IPortalDataCollector> dataCollectors = Collections.emptyMap();
    
    /**
     * @param collectors The {@link IPortalDataCollector}s to get data from 
     */
    public void setCollectors(Collection<IPortalDataCollector> collectors) {
        if (collectors == null || collectors.isEmpty()) {
            this.dataCollectors = Collections.emptyMap();
        }
        else {
            final Map<String, IPortalDataCollector> dataCollectors = new LinkedHashMap<String, IPortalDataCollector>();
            
            for (final IPortalDataCollector dataCollector : collectors) {
                dataCollectors.put(dataCollector.getKey(), dataCollector);
            }
            
            this.dataCollectors = dataCollectors;
        }
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollator#getCollectedData()
     */
    public Map<String, Properties> getCollectedData() {
        return this.getCollectedData(this.dataCollectors.keySet());
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollator#getCollectedData(java.util.Set)
     */
    public Map<String, Properties> getCollectedData(Set<String> keysToCollect) {
        final Map<String, Properties> collectedData = new LinkedHashMap<String, Properties>();
        
        for (final String dataKey : keysToCollect) {
            final IPortalDataCollector portalDataCollector = this.dataCollectors.get(dataKey);
            final Properties data = portalDataCollector.getData();
            collectedData.put(dataKey, data);
        }
        
        return collectedData;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollator#getPossibleDataKeys()
     */
    public Set<String> getPossibleDataKeys() {
        return this.dataCollectors.keySet();
    }

}
