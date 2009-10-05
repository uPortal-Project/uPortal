/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups;

import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.security.IPerson;

/**
 * Defines constants for Groups related classes
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public interface IGroupConstants {
    public final String EVERYONE = IPerson.class.getName();
    public final String CHANNEL_CATEGORIES = IChannelDefinition.class.getName();
    public final String PORTAL_ADMINISTRATORS = EVERYONE + ".PortalAdministrators";
    
    //Search method constants
    public final int IS =1;
    public final int STARTS_WITH=2;
    public final int ENDS_WITH=3;
    public final int CONTAINS=4;

    // Composite group service:
    public final String NODE_SEPARATOR = ".";
}
