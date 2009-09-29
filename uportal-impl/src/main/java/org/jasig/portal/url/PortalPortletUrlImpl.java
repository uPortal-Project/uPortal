/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.url;

import java.util.ArrayList;
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
class PortalPortletUrlImpl extends AbstractPortalUrl implements IPortalPortletUrl {
    private final IPortletWindowId portletWindowId;
    private final ConcurrentMap<String, List<String>> portletParameters = new ConcurrentHashMap<String, List<String>>();
    private WindowState windowState = null;
    private PortletMode portletMode = null;
    private boolean action = false;
    
    
    public PortalPortletUrlImpl(HttpServletRequest request, IUrlGenerator urlGenerator, IPortletWindowId portletWindowId) {
        super(request, urlGenerator);
        Validate.notNull(portletWindowId, "portletWindowId can not be null");
        
        this.portletWindowId = portletWindowId;
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
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return this.urlGenerator.generatePortletUrl(this.request, this, this.portletWindowId);
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
        if (!(object instanceof PortalPortletUrlImpl)) {
            return false;
        }
        PortalPortletUrlImpl rhs = (PortalPortletUrlImpl) object;
        return new EqualsBuilder()
            .appendSuper(super.equals(object))
            .append(this.portletWindowId, rhs.portletWindowId)
            .append(this.windowState, rhs.windowState)
            .append(this.portletMode, rhs.portletMode)
            .append(this.action, rhs.action)
            .isEquals();
    }
}
