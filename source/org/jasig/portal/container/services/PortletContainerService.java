/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services;

import java.util.Properties;
import javax.servlet.ServletConfig;

import org.apache.pluto.services.ContainerService;

/**
 * Defines a service for the Apache Pluto Portlet container.
 * All services must receive a ServletConfig and Properties object.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public interface PortletContainerService extends ContainerService {

    /**
     * Initializes the service using the servlet configuration 
     * and the service properties.
     * @param servletConfig the servlet configuration
     * @param properties the service properties
     * @throws Exception if the initialization fails
     */
    public void init(ServletConfig servletConfig, Properties properties) throws Exception;
    
    /**
     * Destroys the services. 
     * This method allows the service to cleanup any resources.
     * @throws Exception if the destruction fails
     */
    public void destroy() throws Exception;   
}
