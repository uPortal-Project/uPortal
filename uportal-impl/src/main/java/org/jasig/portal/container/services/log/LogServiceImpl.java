/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services.log;

import java.util.Properties;

import javax.servlet.ServletConfig;

import org.apache.pluto.services.log.Logger;
import org.jasig.portal.container.services.PortletContainerService;


/**
 * Implementation of Apache Pluto LogService.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class LogServiceImpl implements PortletContainerService, org.apache.pluto.services.log.LogService {
    
    private ServletConfig servletConfig = null;
    private Properties properties = null;
    
    // PortletContainerService methods
    
    public void init(ServletConfig servletConfig, Properties properties) throws Exception {
        this.servletConfig = servletConfig;
        this.properties = properties;
    }
    
    public void destroy() throws Exception {
        // Nothing to do at this time
    }

    // LogService methods
    
    public Logger getLogger(Class klass) {
        return new LoggerImpl(klass);
    }

    public Logger getLogger(String component) {
        return new LoggerImpl(component);
    }

}
