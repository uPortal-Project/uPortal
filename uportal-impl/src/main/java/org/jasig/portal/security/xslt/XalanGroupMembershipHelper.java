/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.security.xslt;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * Provides a Spring locating facade in front of an actual {@link IXalanGroupMembershipHelper} since the Xalan
 * elements can only instantiate classes directly.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XalanGroupMembershipHelper implements IXalanGroupMembershipHelper {
    private final IXalanGroupMembershipHelper groupMembershipHelper;
    
    public XalanGroupMembershipHelper() {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        this.groupMembershipHelper = (IXalanGroupMembershipHelper)applicationContext.getBean("xalanGroupMembershipHelper", IXalanGroupMembershipHelper.class);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.security.xslt.IXalanGroupMembershipHelper#isChannelDeepMemberOf(java.lang.String, java.lang.String)
     */
    public boolean isChannelDeepMemberOf(String fname, String groupKey) {
        return this.groupMembershipHelper.isChannelDeepMemberOf(fname, groupKey);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.xslt.IXalanGroupMembershipHelper#isUserDeepMemberOf(java.lang.String, java.lang.String)
     */
    public boolean isUserDeepMemberOf(String userName, String groupKey) {
        return this.groupMembershipHelper.isUserDeepMemberOf(userName, groupKey);
    }

}
