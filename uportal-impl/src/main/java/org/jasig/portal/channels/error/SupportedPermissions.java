/* Copyright 2001, 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.channels.error;

import org.jasig.portal.IPermissible;

/**
 * Conveys the single permission available from CError that if granted allows
 * users to see the stack trace button. This allows PermissionsManager to show
 * the permission available from CError and grant it to other users or groups.
 * 
 * @author Mark Boyd, mboyd@sungardsct.com
 * @since uPortal 2.6.
 */
public class SupportedPermissions implements IPermissible
{

    static final String OWNER = "UP_ERROR_CHAN";
    static final String VIEW_ACTIVITY = "VIEW";
    static final String DETAILS_TARGET = "DETAILS";
    
    /* (non-Javadoc)
     * @see org.jasig.portal.IPermissible#getActivityTokens()
     */
    public String[] getActivityTokens()
    {
        return new String[] {VIEW_ACTIVITY}; 
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IPermissible#getActivityName(java.lang.String)
     */
    public String getActivityName(String token)
    {
        return "View";
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IPermissible#getTargetTokens()
     */
    public String[] getTargetTokens()
    {
        return new String[] {DETAILS_TARGET};
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IPermissible#getTargetName(java.lang.String)
     */
    public String getTargetName(String token)
    {
        return "Details";
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IPermissible#getOwnerToken()
     */
    public String getOwnerToken()
    {
        return OWNER;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IPermissible#getOwnerName()
     */
    public String getOwnerName()
    {
        return "CError Channel";
    }
}
