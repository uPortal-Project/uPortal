/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.factory;

import java.util.Map;

import javax.servlet.ServletConfig;

import org.apache.pluto.om.Controller;
import org.apache.pluto.om.ControllerFactory;
import org.apache.pluto.om.Model;

/**
 * Implementation of Apache Pluto ControllerFactory.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ControllerFactoryImpl implements ControllerFactory {
    
    private ServletConfig servletConfig = null;
    private Map properties = null;

    // ControllerFactory methods
    
    public Controller get(Model model) {
        return (Controller)model;
    }
    
    // Factory methods

    public void init(ServletConfig config, Map properties) throws Exception {
        this.servletConfig = config;
        this.properties = properties;
    }

    public void destroy() throws Exception {

    }

}
