/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services.information;

import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.PortletURLProvider;

/**
 * Implementation of Apache Pluto PortletURLProvider.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class PortletURLProviderImpl implements PortletURLProvider {

    private PortletWindow portletWindow;
    private PortletStateManager portletStateManager;

    public PortletURLProviderImpl(PortletWindow portletWindow ) {
        this.portletWindow = portletWindow;
        portletStateManager = new PortletStateManager(portletWindow);
    }
    
    // PortletURLProvider methods

    public void setPortletMode(PortletMode mode) {
	  if ( mode != null ) {	
		 portletStateManager.setNextMode(mode);
	  }		 
    }

    public void setWindowState(WindowState state) {
      if ( state != null ) {	
		 portletStateManager.setNextState(state);
      }		 
    }

    public void setAction() {
		portletStateManager.setAction();
    }

    public void setSecure() {  }

    public void clearParameters() {
		portletStateManager.clearParameters();
    }

    public void setParameters(Map parameters) {
		portletStateManager.setParameters(parameters);
    }

    public String toString() {
       return portletStateManager.getActionURL();
    }

}
