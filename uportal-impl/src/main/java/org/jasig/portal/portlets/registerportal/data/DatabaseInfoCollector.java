/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal.data;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jasig.portal.portlets.registerportal.IPortalDataCollector;
import org.jasig.portal.rdbm.IDatabaseMetadata;

/**
 * Gathers JVM System Properties
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class DatabaseInfoCollector implements IPortalDataCollector {
    private IDatabaseMetadata databaseMetadata;
    
    /**
     * @param databaseMetadata the databaseMetadata to set
     */
    public void setDatabaseMetadata(IDatabaseMetadata databaseMetadata) {
        this.databaseMetadata = databaseMetadata;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getData()
     */
    public Map<String, String> getData() {
        final Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put("jdbcDriver", databaseMetadata.getJdbcDriver());
        
        return data;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getKey()
     */
    public String getKey() {
        return "uPortalDatabase";
    }
}
