/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.om;

import java.util.List;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletPreferences {
    public List<IPortletPreference> getPortletPreferences();
    
    public void setPortletPreferences(List<IPortletPreference> portletPreferences);
}
