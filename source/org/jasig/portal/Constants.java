/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * Houses constants used in the portal code base. Developers, any constants
 * added here should be clearly documented.
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
public class Constants
{

    /**
     * The query parameter name that can be appended to a baseActionUrl along
     * with its value, the fname of a channel, to cause an instance of that
     * channel to appear in focused mode without the user having to subcribe
     * to that channel. Examples are infrastructure channels like
     * CChannelManager and CUserPreferences. They don't reside in a user's
     * layout but are merged in via the fname functionality as needed.
     */
    public static final String FNAME_PARAM = "uP_fname";
    
    /**
     * The request parameter name that can be appended to a baseActionURL along
     * with its value, the locales to which the portal should assign priority.
     * These locales will be a priority for the remainder of a user's session.
     * The value of this parameter should be a comma-delimited list of locale
     * codes.  For example, en_US,ja_JP,de_DE
     */
    public static final String LOCALES_PARAM = "uP_locales";

    /**
     * The name of a category into which automatically published channels from
     * a channel archive are placed. It is expected to be found in the root
     * category and if not found during publishing is automatically created.
     */
    public static final String AUTO_PUBLISH_CATEGORY = "Auto-Published";
}
