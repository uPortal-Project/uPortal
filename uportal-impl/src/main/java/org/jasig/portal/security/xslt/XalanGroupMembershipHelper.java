/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security.xslt;

import org.jasig.portal.spring.locator.XalanGroupMembershipHelperLocator;

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
        this.groupMembershipHelper = XalanGroupMembershipHelperLocator.getXalanGroupMembershipHelper();
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
