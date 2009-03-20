/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
