/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;

import org.apache.pluto.om.common.DescriptionSet;

/**
 * Data structure to support WebApplicationDefinition for
 * marshalling and unmarshalling of web.xml.
 * Not needed by the Pluto container.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class WebResourceCollectionImpl implements Serializable {

    private String webResourceName;
    private DescriptionSet descriptions;
    private String[] urlPatterns; 
    private String[] httpMethods;
    
    public WebResourceCollectionImpl() {
    }
    
    public String getWebResourceName() {
        return webResourceName;
    }
    
    public DescriptionSet getDescriptions() {
        return descriptions;
    }
    
    public String[] getUrlPatterns() {
        return urlPatterns;
    }
    
    public String[] getHttpMethods() {
        return httpMethods;
    }

    public void setWebResourceName(String webResourceName) {
        this.webResourceName = webResourceName;
    }
    
    public void setDescriptions(DescriptionSet descriptions) {
        this.descriptions = descriptions;
    }
    
    public void setUrlPatterns(String[] urlPatterns) {
        this.urlPatterns = urlPatterns;
    }
    
    public void setHttpMethods(String[] httpMethods) {
        this.httpMethods = httpMethods;
    }

}
