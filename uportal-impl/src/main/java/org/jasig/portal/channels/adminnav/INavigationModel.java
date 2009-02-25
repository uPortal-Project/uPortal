/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
     * @return boolean
     */
    public boolean canAccess(IAuthorizationPrincipal ap);
}
