/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.url;

import org.apache.pluto.spi.ResourceURLProvider;

/**
 * Simple handling for resource URL generation
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ResourceUrlProviderImpl implements ResourceURLProvider {
    private String path = null;
    
    public ResourceUrlProviderImpl() {
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.ResourceURLProvider#setAbsoluteURL(java.lang.String)
     */
    public void setAbsoluteURL(String path) {
        this.path = path;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.ResourceURLProvider#setFullPath(java.lang.String)
     */
    public void setFullPath(String path) {
        this.path = path;
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.spi.ResourceURLProvider#toString()
     */
    @Override
    public String toString() {
        return this.path;
    }
}
