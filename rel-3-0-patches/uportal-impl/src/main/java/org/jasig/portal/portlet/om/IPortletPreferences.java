/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.om;

import java.util.List;

/**
 * Holder (join) class for a List of portlet preferences
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletPreferences {
    /**
     * @return The List of PortletPreferences, will not be null
     */
    public List<IPortletPreference> getPortletPreferences();
    
    /**
     * @param portletPreferences The List of PortletPreferences.
     * @throws IllegalArgumentException If portletPreferences is null.
     */
    public void setPortletPreferences(List<IPortletPreference> portletPreferences);
}
