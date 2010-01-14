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

package org.jasig.portal.portlets.registerportal.data;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.portlets.registerportal.IPortalDataCollector;

/**
 * Gathers JVM System Properties
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class JvmDataCollector implements IPortalDataCollector {
    private Set<String> propertiesToCollect;
    
    public JvmDataCollector() {
        this.propertiesToCollect = new LinkedHashSet<String>();
        this.propertiesToCollect.add("java.version");
        this.propertiesToCollect.add("java.vendor");
        this.propertiesToCollect.add("java.vendor.url");
        this.propertiesToCollect.add("java.vm.specification.version");
        this.propertiesToCollect.add("java.vm.specification.vendor");
        this.propertiesToCollect.add("java.vm.specification.name");
        this.propertiesToCollect.add("java.vm.version");
        this.propertiesToCollect.add("java.vm.vendor");
        this.propertiesToCollect.add("java.vm.name");
        this.propertiesToCollect.add("java.specification.version");
        this.propertiesToCollect.add("java.specification.vendor");
        this.propertiesToCollect.add("java.specification.name");
        this.propertiesToCollect.add("java.class.version");
        this.propertiesToCollect.add("java.compiler");
        this.propertiesToCollect.add("os.name");
        this.propertiesToCollect.add("os.arch");
        this.propertiesToCollect.add("os.version");
    }
    
    /**
     * @param propertiesToCollect the propertiesToCollect to set
     */
    public void setPropertiesToCollect(Set<String> propertiesToCollect) {
        this.propertiesToCollect = new LinkedHashSet<String>(propertiesToCollect);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getData()
     */
    public Map<String, String> getData() {
        final Map<String, String> data = new LinkedHashMap<String, String>();
        
        for (final String propertyToCollect : this.propertiesToCollect) {
            data.put(propertyToCollect, System.getProperty(propertyToCollect));
        }
        
        return data;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getKey()
     */
    public String getKey() {
        return "JVMProperties";
    }

}
