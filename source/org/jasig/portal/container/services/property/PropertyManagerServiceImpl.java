/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.container.services.property;

import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;

import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.property.PropertyManagerService;


/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision $
 */
public class PropertyManagerServiceImpl implements PropertyManagerService {
    private final Map propertyMapping = new Hashtable();
    
    /**
     * @see org.apache.pluto.services.property.PropertyManagerService#setResponseProperties(org.apache.pluto.om.window.PortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Map)
     */
    public void setResponseProperties(PortletWindow window, HttpServletRequest request,
                                      HttpServletResponse response, Map properties) {
        propertyMapping.put(window.getId(), properties);
    }

    /**
     * @see org.apache.pluto.services.property.PropertyManagerService#getRequestProperties(org.apache.pluto.om.window.PortletWindow, javax.servlet.http.HttpServletRequest)
     */
    public Map getRequestProperties(PortletWindow window, HttpServletRequest request) {
        Map properties = new Properties();
        Map savedProps = (Map)propertyMapping.get(window.getId());
        
        
        //uPortal extension: Provide the expiration cache time value        
        String[] exprTime = null;
        if (savedProps != null)
            exprTime = (String[])savedProps.get(RenderResponse.EXPIRATION_CACHE);
        
        if (exprTime == null) {
            PortletEntity pe = window.getPortletEntity();
            PortletDefinition pd = pe.getPortletDefinition();
            
            //Values MUST be String[]
            exprTime = new String[] {pd.getExpirationCache()};
        } 
        properties.put(RenderResponse.EXPIRATION_CACHE, exprTime);
        //End uPortal extension
                        
        return properties;
    }

}
