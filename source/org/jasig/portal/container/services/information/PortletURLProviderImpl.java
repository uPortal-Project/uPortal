/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.container.services.information;

import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.information.PortletURLProvider;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.container.servlet.ServletRequestImpl;

/**
 * Implementation of Apache Pluto PortletURLProvider.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class PortletURLProviderImpl implements PortletURLProvider {

    private DynamicInformationProviderImpl provider;
    private PortletWindow portletWindow;
    private PortletMode portletMode;
    private WindowState windowState;
    private boolean action;
    private boolean secure;
    private boolean clearParameters;
    private Map parameters;
    private PortalControlParameter controlURL;

    public PortletURLProviderImpl(DynamicInformationProviderImpl provider, PortletWindow portletWindow, PortalControlParameter controlURL ) {
        this.provider = provider;
        this.portletWindow = portletWindow;
        this.controlURL = controlURL;
		this.controlURL.setPortletId(portletWindow);
    }
    
    // PortletURLProvider methods

    public void setPortletMode(PortletMode mode) {
	  if ( mode != null && !controlURL.getMode(portletWindow).equals(mode) ) {	
		 this.portletMode = mode;
		 controlURL.setMode(portletWindow, portletMode);
	  }		 
    }

    public void setWindowState(WindowState state) {
      if ( state != null && !controlURL.getState(portletWindow).equals(state) ) {	
         this.windowState = state;
		 controlURL.setState(portletWindow, windowState);
      }		 
    }

    public void setAction() {
    	action = true;
		controlURL.setAction(portletWindow);
    }

    public void setSecure() {
        secure = true;
    }

    public void clearParameters() {
        clearParameters = true;
		controlURL.clearRenderParameters(portletWindow);
    }

    public void setParameters(Map parameters) {
        this.parameters = parameters;
    }

    public String toString() {
    	
        ChannelRuntimeData runtimeData = ((ServletRequestImpl)provider.getServletRequest()).getChannelRuntimeData();
        String baseActionURL = runtimeData.getBaseActionURL();
		StringBuffer url = new StringBuffer(baseActionURL);
	
        if ( parameters != null && !parameters.isEmpty() ) {        
         Iterator names = parameters.keySet().iterator();
         boolean firstValue = true;
         while (names.hasNext()) {
         	
            String name = (String)names.next();
            Object value = parameters.get(name);
			String[] values = ( value instanceof String ) ? new String[] {(String)value} : (String[])value;
			if (action)
				controlURL.setRequestParam(name,values);
            else
				controlURL.setRenderParam(portletWindow, name, values );
         }
        } 
        String curl = controlURL.toString();
		if ( curl.length() > 0 ) url.append("?"+curl);
        return url.toString();
    }

}
