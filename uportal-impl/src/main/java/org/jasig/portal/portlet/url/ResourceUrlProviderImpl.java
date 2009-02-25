/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
