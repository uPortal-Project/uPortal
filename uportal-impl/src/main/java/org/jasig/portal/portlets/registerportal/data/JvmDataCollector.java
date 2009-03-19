/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal.data;

import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;

import org.jasig.portal.portlets.registerportal.IPortalDataCollector;

/**
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
    public Properties getData() {
        final Properties data = new Properties();
        
        for (final String propertyToCollect : this.propertiesToCollect) {
            data.setProperty(propertyToCollect, System.getProperty(propertyToCollect));
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
