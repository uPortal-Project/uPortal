/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.channels.adminnav;

import org.jasig.portal.ICacheable;
import org.jasig.portal.IChannel;
import org.jasig.portal.security.IAuthorizationPrincipal;

/**
 * Represents a pluggable facility for implementing the administrative navigation
 * channel's functionality.
 * 
 * @author mboyd@sungardsct.com
 */
public interface INavigationModel extends ICacheable, IChannel, ILinkRegistrar
{
    /**
     * Answers true if the user represented by the passed-in authorization
     * principal can access any of the channels pointed
     *   
     * @param ap
     * @return
     */
    public boolean canAccess(IAuthorizationPrincipal ap);
}
