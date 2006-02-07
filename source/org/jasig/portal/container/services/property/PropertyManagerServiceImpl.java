/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.container.services.property;

import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

import javax.portlet.RenderResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.services.property.PropertyManagerService;


/**
 * The {@link PropertyManagerServiceImpl} is used to pass properties to the
 * portlet so it can read them via it's {@link javax.portlet.PortletRequest#getProperty(java.lang.String)}
 * methods and so properties set by the portlet via it's {@link javax.portlet.PortletResponse#setProperty(java.lang.String, java.lang.String)}
 * methods can be read by uPortal.
 * 
 * Currently all properties set by the portlet are saved into a {@link WeakHashMap}
 * using the {@link org.apache.pluto.om.window.PortletWindow} as the key. This
 * should ensure that the old properties aren't stored beyond the life of the
 * user's session.
 * 
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision$
 */
public class PropertyManagerServiceImpl implements PropertyManagerService {
    private final Map propertyMapping = new WeakHashMap();
    protected Log log = LogFactory.getLog(getClass());
    
    /**
     * Stores the properties in a {@link WeakHashMap} that is keyed off the
     * {@link PortletWindow} so the properties are removed when the user's
     * session with the portlet is done.
     * 
     * @see org.apache.pluto.services.property.PropertyManagerService#setResponseProperties(org.apache.pluto.om.window.PortletWindow, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.util.Map)
     */
    public void setResponseProperties(final PortletWindow window, final HttpServletRequest request,
                                      final HttpServletResponse response, final Map properties) {
        synchronized (propertyMapping) {
            propertyMapping.put(window, properties);
        }
    }


    /**
     * Gets the properties that have been set by the portlet. This will pass
     * any properties that the portlet has set in the response back into the
     * portlet's request.
     * 
     * This code also provides a uPortal extension that provides the current
     * cache expiration time for the portlet via the {@link RenderResponse#EXPIRATION_CACHE}
     * constant.
     * 
     * @see org.apache.pluto.services.property.PropertyManagerService#getRequestProperties(org.apache.pluto.om.window.PortletWindow, javax.servlet.http.HttpServletRequest)
     */
    public Map getRequestProperties(final PortletWindow window, final HttpServletRequest request) {
        final Map properties = new Properties();
        Map savedProps = null;
        
        synchronized (propertyMapping) {
            savedProps = (Map)propertyMapping.get(window);
        }
        
        //Copy all the properties into a new map to return.
        if (savedProps != null)
            properties.putAll(savedProps);
        
        
        //Make sure the EXPIRATION_CACHE property is set to whatever the current
        //cache timeout for the portlet is. This is not a required property
        final String[] exprTime = (String[])properties.get(RenderResponse.EXPIRATION_CACHE);
        if (exprTime == null) {
            final PortletEntity pe = window.getPortletEntity();
            final PortletDefinition pd = pe.getPortletDefinition();
            String value = pd.getExpirationCache();
            try {
                Integer.parseInt(value);
            }
            catch (NumberFormatException e) {
            	log.warn("Expriation cache \""+value+"\" must be an integer in window "+window+". " +
            			"Using -1 instead.",e);
                value = "-1";
            }
            
            //Values MUST be String[]
            properties.put(RenderResponse.EXPIRATION_CACHE, new String[] {value});
        }
                        
        return properties;
    }
}
