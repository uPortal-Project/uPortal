/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;

/**
 * Data structure to support WebApplicationDefinition for
 * marshalling and unmarshalling of web.xml.
 * Not needed by the Pluto container.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class FilterMappingImpl implements Serializable {

    private String filterName;
    private String urlPattern;
    private String servletName;
    
    public String getFilterName() {
        return filterName;
    }

    public String getServletName() {
        return servletName;
    }

    public String getUrlPattern() {
        return urlPattern;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public void setServletName(String servletName) {
        this.servletName = servletName;
    }

    public void setUrlPattern(String urlPattern) {
        this.urlPattern = urlPattern;
    }

}
