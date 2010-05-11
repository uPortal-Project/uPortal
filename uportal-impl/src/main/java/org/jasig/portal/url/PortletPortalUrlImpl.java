/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.jasig.portal.portlet.om.IPortletWindowId;

/**
 * Implementation of a portlet URL
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
class PortletPortalUrlImpl extends AbstractPortalUrl implements IPortletPortalUrl {
    private final IPortletWindowId portletWindowId;
    private final String channelSubscribeId;
    
    private final ConcurrentMap<String, List<String>> portletParameters = new ConcurrentHashMap<String, List<String>>();
    private WindowState windowState = null;
    private PortletMode portletMode = null;
    private boolean action = false;
    
    
    public PortletPortalUrlImpl(HttpServletRequest request, IUrlGenerator urlGenerator, IPortletWindowId portletWindowId) {
        super(request, urlGenerator);
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        this.portletWindowId = portletWindowId;
        this.channelSubscribeId = null;
    }
    
    public PortletPortalUrlImpl(HttpServletRequest request, IUrlGenerator urlGenerator, String channelSubscribeId) {
        super(request, urlGenerator);
        Validate.notNull(channelSubscribeId, "portletWindowId can not be null");
        
        this.portletWindowId = null;
        this.channelSubscribeId = channelSubscribeId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalPortletUrl#getPortletMode()
     */
    public PortletMode getPortletMode() {
        return this.portletMode;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalPortletUrl#getPortletParameters()
     */
    public Map<String, List<String>> getPortletParameters() {
        return this.portletParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalPortletUrl#getWindowState()
     */
    public WindowState getWindowState() {
        return this.windowState;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalPortletUrl#isAction()
     */
    public boolean isAction() {
        return this.action;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalPortletUrl#setAction(boolean)
     */
    public void setAction(boolean action) {
        this.action = action;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalPortletUrl#setPortletMode(javax.portlet.PortletMode)
     */
    public void setPortletMode(PortletMode portletMode) {
        this.portletMode = portletMode;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalPortletUrl#addPortletParameter(java.lang.String, java.lang.String[])
     */
    public void addPortletParameter(String name, String... values) {
        Validate.notNull(name, "name can not be null");
        Validate.noNullElements(values, "values can not be null or contain null elements");
        
        List<String> valuesList = this.portletParameters.get(name);
        if (valuesList == null) {
            valuesList = new ArrayList<String>(values.length);
        }
        
        for (final String value : values) {
            valuesList.add(value);
        }
        
        this.portletParameters.put(name, valuesList);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalPortletUrl#setPortletParameter(java.lang.String, java.lang.String[])
     */
    public void setPortletParameter(String name, String... values) {
        Validate.notNull(name, "name can not be null");
        Validate.noNullElements(values, "values can not be null or contain null elements");
        
        final List<String> valuesList = new ArrayList<String>(values.length);
        for (final String value : values) {
            valuesList.add(value);
        }
        
        this.portletParameters.put(name, valuesList);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalPortletUrl#setPortletParameters(java.util.Map)
     */
    public void setPortletParameters(Map<String, List<String>> parameters) {
        this.portletParameters.clear();
        this.portletParameters.putAll(parameters);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.url.IPortalPortletUrl#setWindowState(javax.portlet.WindowState)
     */
    public void setWindowState(WindowState windowState) {
        this.windowState = windowState;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.url.IBasePortalUrl#getUrlString()
     */
    public String getUrlString() {
        if (this.portletWindowId != null) {
            return this.urlGenerator.generatePortletUrl(this.request, this, this.portletWindowId);
        }
        
        return this.urlGenerator.generatePortletUrl(this.request, this, this.channelSubscribeId);
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.getUrlString();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-942605321, 2130461357)
            .appendSuper(super.hashCode())
            .append(this.portletWindowId)
            .append(this.windowState)
            .append(this.portletMode)
            .append(this.action)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof PortletPortalUrlImpl)) {
            return false;
        }
        PortletPortalUrlImpl rhs = (PortletPortalUrlImpl) object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(this.portletWindowId, rhs.portletWindowId)
            .append(this.windowState, rhs.windowState)
            .append(this.portletMode, rhs.portletMode)
            .append(this.action, rhs.action)
            .isEquals();
    }

    /**
     * 
     */
	public void addPortletParameter(String name, String value) {
		this.portletParameters.put(name, Arrays.asList(new String[] { value }));
	}
}
