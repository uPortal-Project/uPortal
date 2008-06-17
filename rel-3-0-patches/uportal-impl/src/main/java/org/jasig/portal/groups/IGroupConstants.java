/* Copyright 2001, 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;

/**
 * Defines constants for Groups related classes
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public interface IGroupConstants {
    public final String EVERYONE = "org.jasig.portal.security.IPerson";
    public final String CHANNEL_CATEGORIES = "org.jasig.portal.ChannelDefinition";
    public final String PORTAL_ADMINISTRATORS = "org.jasig.portal.security.IPerson.PortalAdministrators";
    
    //Search method constants
    public final int IS =1;
    public final int STARTS_WITH=2;
    public final int ENDS_WITH=3;
    public final int CONTAINS=4;

    // Composite group service:
    public final String NODE_SEPARATOR = ".";
}
