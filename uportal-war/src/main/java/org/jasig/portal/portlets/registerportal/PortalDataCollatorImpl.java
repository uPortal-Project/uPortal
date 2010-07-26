/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlets.registerportal;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
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
    public Map<String, Map<String, String>> getCollectedData() {
        return this.getCollectedData(this.dataCollectors.keySet());
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollator#getCollectedData(java.util.Set)
     */
    public Map<String, Map<String, String>> getCollectedData(Set<String> keysToCollect) {
        final Map<String, Map<String, String>> collectedData = new LinkedHashMap<String, Map<String, String>>();
        
        for (final String dataKey : keysToCollect) {
            final IPortalDataCollector portalDataCollector = this.dataCollectors.get(dataKey);
            final Map<String, String> data = portalDataCollector.getData();
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
