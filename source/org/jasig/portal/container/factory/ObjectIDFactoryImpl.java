/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.factory;

import java.util.Map;

import javax.servlet.ServletConfig;

import org.apache.pluto.factory.ObjectIDFactory;
import org.apache.pluto.om.common.ObjectID;
import org.jasig.portal.container.om.common.ObjectIDImpl;

/**
 * Implementation of Apache Pluto ObjectIDFactory.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ObjectIDFactoryImpl implements ObjectIDFactory {
    
    private ServletConfig servletConfig = null;
    private Map properties = null;

    // ObjectIDFactory methods
    
    public ObjectID createObjectID(String portletGUID) {
        return ObjectIDImpl.createFromString(portletGUID);
    }

    // Factory methods
    
    public void init(ServletConfig config, Map properties) throws Exception {
        this.servletConfig = config;
        this.properties = properties;
    }

    public void destroy() throws Exception {

    }

}
