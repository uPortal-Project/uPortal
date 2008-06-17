/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.properties;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.om.IPortletWindow;


/**
 * Delegates properties related calls to a {@link List} of {@link IRequestPropertiesManager}s in order. No
 * exception handling is done so any exception in a child IRequestPropertiesManager will cause the call to fail.
 * <br/>
 * <br/>
 * When {@link #getRequestProperties(HttpServletRequest, IPortletWindow)} is called the returned Maps are overlayed
 * in order to produce the final Map that is returned. This means IRequestPropertiesManagers lower in the
 * propertiesManagers List can over-write properties from IRequestPropertiesManagers higher in the list.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class RequestPropertiesManagerBroker implements IRequestPropertiesManager {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private List<IRequestPropertiesManager> propertiesManagers = Collections.emptyList();
    
    /**
     * @return the propertiesManagers
     */
    public List<IRequestPropertiesManager> getPropertiesManagers() {
        return propertiesManagers;
    }

    /**
     * @param propertiesManagers the propertiesManagers to set
     */
    public void setPropertiesManagers(List<IRequestPropertiesManager> propertiesManagers) {
        Validate.notNull(propertiesManagers, "propertiesManagers can not be null");
        this.propertiesManagers = propertiesManagers;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.IRequestPropertiesManager#addResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)
     */
    public void addResponseProperty(HttpServletRequest request, IPortletWindow portletWindow, String property, String value) {
        for (final IRequestPropertiesManager propertiesManager : this.propertiesManagers) {
            propertiesManager.addResponseProperty(request, portletWindow, property, value);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.IRequestPropertiesManager#setResponseProperty(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, java.lang.String, java.lang.String)
     */
    public void setResponseProperty(HttpServletRequest request, IPortletWindow portletWindow, String property, String value) {
        for (final IRequestPropertiesManager propertiesManager : this.propertiesManagers) {
            propertiesManager.setResponseProperty(request, portletWindow, property, value);
        }
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlet.container.services.IRequestPropertiesManager#getRequestProperties(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow)
     */
    public Map<String, String[]> getRequestProperties(HttpServletRequest request, IPortletWindow portletWindow) {
        final Map<String, String[]> properties = new HashMap<String, String[]>();
        
        for (final IRequestPropertiesManager propertiesManager : this.propertiesManagers) {
            final Map<String, String[]> newProperties = propertiesManager.getRequestProperties(request, portletWindow);
            
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Retrieved properties '" + newProperties + "' from manager: " + propertiesManager);
            }
            
            properties.putAll(newProperties);
        }
        
        if (this.logger.isDebugEnabled()) {
            this.logger.debug("Returning properties '" + properties + "' for portlet " +  portletWindow + " and request " + request);
        }
        
        return properties;
    }
}
