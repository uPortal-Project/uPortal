/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security.xslt;

/**
 * Group Membership Helper APIs
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IXalanGroupMembershipHelper {
    /**
     * Checks if the user is a deep member of the specified group
     * 
     * @param userName Name of the user to check
     * @param groupKey internal group key (ex: local.0)
     * @return true if the user is a deep member, false otherwise
     */
    public boolean isUserDeepMemberOf(String userName, String groupKey);
    
    /**
     * Checks if the channel is a deep member of the specified group
     * 
     * @param fname FName of the channel
     * @param groupKey internal group key (ex: local.0)
     * @return true if the channel is a deep member, false otherwise
     */
    public boolean isChannelDeepMemberOf(String fname, String groupKey);

}
