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

package org.jasig.portal.channels.portlet;

import java.util.Map;

import javax.portlet.PortletMode;

import org.apache.pluto.om.window.PortletWindow;

/**
 * An object that keeps track of session data.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ChannelData {
    private boolean portletWindowInitialized = false;
    private PortletWindow portletWindow = null;
    private Map userInfo = null;
    private boolean processedAction = false;
    private boolean receivedEvent = false;
    private boolean focused = false;
    private PortletMode newPortletMode = null;
    private Map lastRequestParameters = null;
        
    public boolean isPortletWindowInitialized() { return this.portletWindowInitialized; }
    public PortletWindow getPortletWindow() { return this.portletWindow; }
    public Map getUserInfo() { return this.userInfo; }
    public boolean hasProcessedAction() { return this.processedAction; }
    public boolean hasReceivedEvent() { return this.receivedEvent; }
    public boolean isFocused() { return this.focused; }
    public PortletMode getNewPortletMode() { return this.newPortletMode; }
    public Map getLastRequestParameters() { return this.lastRequestParameters; }
        
    public void setPortletWindowInitialized(boolean portletWindowInitialized) { this.portletWindowInitialized = portletWindowInitialized; }
    public void setPortletWindow(PortletWindow portletWindow) { this.portletWindow = portletWindow; }
    public void setUserInfo(Map userInfo) { this.userInfo = userInfo; }
    public void setProcessedAction(boolean processedAction) { this.processedAction = processedAction; }
    public void setReceivedEvent(boolean receivedEvent) { this.receivedEvent = receivedEvent; }
    public void setFocused(boolean focused) { this.focused = focused; }
    public void setNewPortletMode(PortletMode newPortletMode) { this.newPortletMode = newPortletMode; }
    public void setLastRequestParameters(Map lastRequestParameters) { this.lastRequestParameters = lastRequestParameters; }
}

