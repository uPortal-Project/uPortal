/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal.data;

import org.jasig.portal.portlets.registerportal.IPortalDataCollector;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class JvmDataCollector implements IPortalDataCollector<String> {

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getData()
     */
    public String getData() {
        return System.getProperty("java.version") + ", " + System.getProperty("java.vendor") + ", " + System.getProperty("java.vendor.url");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getKey()
     */
    public String getKey() {
        return "JVMProperties";
    }

}
