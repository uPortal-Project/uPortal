/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal;

import javax.servlet.http.HttpSessionBindingListener;

import org.jasig.portal.i18n.LocaleManager;
import org.jasig.portal.security.IPerson;

/**
 * Provides access to the layout and rendering related services for a portal user.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IUserInstance extends HttpSessionBindingListener {
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
}
