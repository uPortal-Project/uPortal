/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.security.xslt;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * Provides a Spring locating facade in front of an actual {@link IXalanAuthorizationHelper} since the Xalan
 * elements can only instantiate classes directly.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XalanAuthorizationHelper implements IXalanAuthorizationHelper {
    private final IXalanAuthorizationHelper authorizationHelper;
    
    @SuppressWarnings("deprecation")
    public XalanAuthorizationHelper() {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        this.authorizationHelper = (IXalanAuthorizationHelper)applicationContext.getBean("xalanAuthorizationHelper", IXalanAuthorizationHelper.class);
    }

    /**
     * @see org.jasig.portal.security.xslt.IXalanAuthorizationHelper#canRender(java.lang.String, java.lang.String)
     */
    public boolean canRender(final String userName, final String channelFName) {
        return this.authorizationHelper.canRender(userName, channelFName);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.xslt.IXalanAuthorizationHelper#isMemberOf(java.lang.String, java.lang.String)
     */
    public boolean isMemberOf(String userName, String groupKey) {
        return this.authorizationHelper.isMemberOf(userName, groupKey);
    }
}
