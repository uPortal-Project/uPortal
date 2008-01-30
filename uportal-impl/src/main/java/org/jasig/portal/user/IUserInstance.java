/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.user;

import javax.servlet.http.HttpSession;

import org.jasig.portal.ChannelManager;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.IPerson;

/**
 * Provides access to the layout and rendering related services for a portal user.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IUserInstance {
    /**
     * @return The Person this UserInstance is for
     */
    public IPerson getPerson();

    /**
     * @return The user preferences manager for the user instance
     */
    public IUserPreferencesManager getPreferencesManager();

    /**
     * @return the channelManager for the user instance
     */
    public ChannelManager getChannelManager();

    /**
     * @return the localeManager for the user instance
     */
    public LocaleManager getLocaleManager();

    /**
     * @return the renderingLock for the user instance
     */
    public Object getRenderingLock();
    
    /**
     * Notify the instance and all its members that the user's session is being destroyed.
     * 
     * @param session The session that was just destroyed for this user instance.
     */
    public void destroySession(HttpSession session);
}
