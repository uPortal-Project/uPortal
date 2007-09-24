/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;

/**
 * Data structure for each <servlet-mapping> element
 * in the portlet web.xml file. Used in ServletDefinitionImpl.
 * Pluto's ServletMappingImpl had an id field, which didn't 
 * appear to be used anywhere.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ServletMappingImpl implements Serializable {

    private String servletName;
    private String urlPattern;
    
    public ServletMappingImpl() {
        
    }
    
    public ServletMappingImpl(String servletName, String urlPattern) {
        this();
        setServletName(servletName);
        setUrlPattern(urlPattern);
    }

    public String getServletName() {
        return servletName;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

}
