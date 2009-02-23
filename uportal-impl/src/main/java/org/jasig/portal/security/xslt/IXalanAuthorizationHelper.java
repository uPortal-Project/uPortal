/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.security.xslt;

/**
 * Authorization helper APIs.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IXalanAuthorizationHelper {
    /**
     * Checks if the specified user can render the specified channel.
     * 
     * @param userName Looks up the user with the matching {@link org.jasig.portal.security.IPerson#USERNAME}
     * @param channelFName Looks up the {@link org.jasig.portal.ChannelDefinition} with the matching fname
     * @return true if the user has permission to render the channel, false for any other case.
     */
    public boolean canRender(final String userName, final String channelFName);
    
    /**
     * Checks if the user is a member of the specified group
     * 
     * @param userName Name of the user to check
     * @param groupKey interal group key (ex: local.0)
     * @return true if the user is a member, false otherwise
     */
    public boolean isMemberOf(String userName, String groupKey);
}
