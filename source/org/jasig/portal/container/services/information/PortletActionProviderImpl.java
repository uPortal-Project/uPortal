/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services.information;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.PortletActionProvider;

/**
 * Implementation of Apache Pluto PortletActionProvider.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class PortletActionProviderImpl implements PortletActionProvider {
    
    private PortletWindow portletWindow;

    public PortletActionProviderImpl(PortletWindow portletWindow) {
        this.portletWindow = portletWindow;
    }

    // PortletActionProvider methods
    public void changePortletMode(PortletMode mode) {	
		if ( mode != null ) {
			PortletStateManager.setMode(portletWindow,mode);
		}  
    }

    public void changePortletWindowState(WindowState state) {
		if ( state != null ) {	
		    PortletStateManager.setState(portletWindow,state);
	    }  
    }

}
