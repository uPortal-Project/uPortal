/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services.information;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Collection;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderAccess;
import org.apache.pluto.services.information.StaticInformationProvider;
import org.apache.pluto.services.information.PortletActionProvider;
import org.apache.pluto.services.information.PortletURLProvider;
import org.apache.pluto.services.information.ResourceURLProvider;


/**
 * Implementation of Apache Pluto DynamicInformationProvider.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class DynamicInformationProviderImpl implements DynamicInformationProvider {
	
    private static StaticInformationProvider staticInfoProvider;
    
    public DynamicInformationProviderImpl() {
        if ( staticInfoProvider == null )
		 staticInfoProvider = InformationProviderAccess.getStaticProvider();
    }

    // DynamicInformationProvider methods
      
    public String getResponseContentType() {
        return  "text/html";
    }

    public Iterator getResponseContentTypes() {
        Set responseMimeTypes = new HashSet(1); // number of known mime types
        responseMimeTypes.add(getResponseContentType());
        return responseMimeTypes.iterator();
    }


	public PortletStateManager getPortletStateManager(PortletWindow portletWindow) {
		return new PortletStateManager(portletWindow);
	}

    public PortletURLProvider getPortletURLProvider(PortletWindow portletWindow) {
        return new PortletURLProviderImpl(portletWindow);
    }

    public ResourceURLProvider getResourceURLProvider(PortletWindow portletWindow) {
        return new ResourceURLProviderImpl(portletWindow);
    }

    public PortletActionProvider getPortletActionProvider(PortletWindow portletWindow) {
        return new PortletActionProviderImpl(portletWindow);
    }

    public PortletMode getPortletMode(PortletWindow portletWindow) {
        return PortletStateManager.getMode(portletWindow);
    }

    public PortletMode getPreviousPortletMode(PortletWindow portletWindow) {
        return PortletStateManager.getPrevMode(portletWindow);
    }

    public WindowState getWindowState(PortletWindow portletWindow) {
        return PortletStateManager.getState(portletWindow);
    }

    public WindowState getPreviousWindowState(PortletWindow portletWindow) {
        return PortletStateManager.getPrevState(portletWindow);
    }

    public boolean isPortletModeAllowed(PortletMode mode) {
    	Collection supportedModes = staticInfoProvider.getPortalContextProvider().getSupportedPortletModes();
		return supportedModes.contains(mode);
    }

    public boolean isWindowStateAllowed(WindowState state) {
		Collection supportedStates = staticInfoProvider.getPortalContextProvider().getSupportedWindowStates();
		return supportedStates.contains(state);
    }

}
