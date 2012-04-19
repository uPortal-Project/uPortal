/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlet.delegation.jsp;

import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.PortletSession;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;
import javax.portlet.WindowState;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.Tag;
import javax.servlet.jsp.tagext.TagData;
import javax.servlet.jsp.tagext.TagExtraInfo;
import javax.servlet.jsp.tagext.TagSupport;
import javax.servlet.jsp.tagext.ValidationMessage;

import org.jasig.portal.api.portlet.DelegateState;
import org.jasig.portal.api.portlet.DelegationRequest;
import org.jasig.portal.api.portlet.PortletDelegationDispatcher;
import org.jasig.portal.api.portlet.PortletDelegationLocator;
import org.jasig.portal.portlet.PortletUtils;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RenderPortletTag extends TagSupport {
    private static final long serialVersionUID = 1L;
    
    public static final String DEFAULT_SESSION_KEY_PREFIX = "DELEGATE_WINDOW_ID__";


    public static class TEI extends TagExtraInfo {
        @Override
        public ValidationMessage[] validate(TagData data) {
            final List<ValidationMessage> messages = new ArrayList<ValidationMessage>();
            
            Object o = data.getAttribute("fname");
            if (o == null) {
                messages.add(new ValidationMessage(data.getId(), "fname cannot be null."));
            }

            if (messages.size() == 0) {
                return null;
            }
            
            return messages.toArray(new ValidationMessage[messages.size()]);
        }

    }
    
    private String sessionKeyPrefix = DEFAULT_SESSION_KEY_PREFIX;
    private String fname = null;
    private String windowState = null;
    private String portletMode = null;
    

    private transient WindowState parentUrlState = null;
    private transient PortletMode parentUrlMode = null;
    private Map<String, List<String>> parentUrlParameters = null;
    

    public String getSessionKeyPrefix() {
        return this.sessionKeyPrefix;
    }
    public void setSessionKeyPrefix(String windowSessionKey) {
        this.sessionKeyPrefix = windowSessionKey;
    }

    public String getFname() {
        return this.fname;
    }
    public void setFname(String fname) {
        this.fname = fname;
    }
    
    public String getWindowState() {
        return this.windowState;
    }
    public void setWindowState(String windowState) {
        this.windowState = windowState;
    }
    
    public String getPortletMode() {
        return this.portletMode;
    }
    public void setPortletMode(String portletMode) {
        this.portletMode = portletMode;
    }
    
    
    @Override
    public int doStartTag() throws JspException {
        return EVAL_BODY_INCLUDE;
    }

    @Override
    public int doEndTag() throws JspException {
        //From portlet:defineObjects
        final RenderRequest renderRequest = (RenderRequest)this.pageContext.getAttribute("renderRequest");
        final RenderResponse renderResponse = (RenderResponse)this.pageContext.getAttribute("renderResponse");
        
        final PortletDelegationLocator portletDelegationLocator = (PortletDelegationLocator)renderRequest.getAttribute(PortletDelegationLocator.PORTLET_DELECATION_LOCATOR_ATTR);
        
        final String sessionKey = this.sessionKeyPrefix + this.fname;

        final PortletSession portletSession = renderRequest.getPortletSession();
        IPortletWindowId portletWindowId = (IPortletWindowId)portletSession.getAttribute(sessionKey);

        final PortletDelegationDispatcher portletDelegationDispatcher;
        final DelegateState delegateState;
        //No id in session, create a new dispatcher
        if (portletWindowId == null) {
            portletDelegationDispatcher = portletDelegationLocator.createRequestDispatcher(renderRequest, this.fname);
            portletWindowId = portletDelegationDispatcher.getPortletWindowId();
            portletSession.setAttribute(sessionKey, portletWindowId);

            final PortletMode portletMode = PortletUtils.getPortletMode(this.portletMode);
            final WindowState windowState = PortletUtils.getWindowState(this.windowState);
            
            delegateState = new DelegateState(portletMode, windowState);
        }
        //id in session, get the old dispatcher
        else {
            portletDelegationDispatcher = portletDelegationLocator.getRequestDispatcher(renderRequest, portletWindowId);
            delegateState = null;
        }

        final DelegationRequest delegationRequest = new DelegationRequest();
        delegationRequest.setDelegateState(delegateState);
        
        //Setup base for portlet URLs
        delegationRequest.setParentPortletMode(this.parentUrlMode);
        delegationRequest.setParentWindowState(this.parentUrlState);
        delegationRequest.setParentParameters(this.parentUrlParameters);

        final JspWriter out = this.pageContext.getOut();
        try {
            portletDelegationDispatcher.doRender(renderRequest, renderResponse, delegationRequest, new JspWriterPortletOutputHandler(out));
        }
        catch (IOException e) {
            throw new JspException("Failed to execute delegate render on portlet '" + this.fname + "'", e);
        }
        
        return Tag.EVAL_PAGE;
    }
    

    protected void setParentUrlState(WindowState parentUrlState) {
        this.parentUrlState = parentUrlState;
    }
    protected void setParentUrlMode(PortletMode parentUrlMode) {
        this.parentUrlMode = parentUrlMode;
    }
    protected void setParentUrlParameters(Map<String, List<String>> parentUrlParameters) {
        this.parentUrlParameters = parentUrlParameters;
    }
    


    private void writeObject(ObjectOutputStream oos) throws IOException {
        oos.defaultWriteObject();
        oos.writeObject(this.parentUrlState.toString());
        oos.writeObject(this.parentUrlMode.toString());
    }
    
    private void readObject(ObjectInputStream ois) throws IOException, ClassNotFoundException {
        //Read & validate non-transient fields
        ois.defaultReadObject();
        
        
        //Read & validate transient fields
        final String portletModeStr = (String)ois.readObject();
        if (portletModeStr == null) {
            throw new InvalidObjectException("portletMode can not be null");
        }
        this.parentUrlMode = PortletUtils.getPortletMode(portletModeStr);
        
        final String windowStateStr = (String)ois.readObject();
        if (windowStateStr == null) {
            throw new InvalidObjectException("windowState can not be null");
        }
        this.parentUrlState = PortletUtils.getWindowState(windowStateStr);
    }
}
