/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security.xslt;

import org.jasig.portal.spring.locator.XalanAuthorizationHelperLocator;

/**
 * Provides a Spring locating facade in front of an actual {@link IXalanAuthorizationHelper} since the Xalan
 * elements can only instantiate classes directly.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XalanAuthorizationHelper implements IXalanAuthorizationHelper {
    private final IXalanAuthorizationHelper authorizationHelper;
    
    public XalanAuthorizationHelper() {
        this.authorizationHelper = XalanAuthorizationHelperLocator.getXalanAuthorizationHelper();
    }

    /**
     * @see org.jasig.portal.security.xslt.IXalanAuthorizationHelper#canRender(java.lang.String, java.lang.String)
     */
    public boolean canRender(final String userName, final String channelFName) {
        return this.authorizationHelper.canRender(userName, channelFName);
    }
}
