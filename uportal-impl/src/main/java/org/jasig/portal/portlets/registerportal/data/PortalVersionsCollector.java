/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal.data;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jasig.portal.portlets.registerportal.IPortalDataCollector;
import org.jasig.portal.tools.versioning.Version;
import org.jasig.portal.tools.versioning.VersionsManager;

/**
 * Gathers JVM System Properties
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalVersionsCollector implements IPortalDataCollector {
    
    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getData()
     */
    public Map<String, String> getData() {
        final VersionsManager versionsManager = VersionsManager.getInstance();
        
        final Map<String, String> data = new LinkedHashMap<String, String>();
        
        final Version[] versions = versionsManager.getVersions();
        for (final Version version : versions) {
            data.put(version.getFname(), version.dottedTriple());
        }
        
        return data;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getKey()
     */
    public String getKey() {
        return "uPortalVersions";
    }
}
