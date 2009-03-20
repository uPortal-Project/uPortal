/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.portlets.registerportal.data;

import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.ServletContext;

import org.jasig.portal.portlets.registerportal.IPortalDataCollector;
import org.springframework.web.context.ServletContextAware;

/**
 * Gathers servlet container info
 * 
 * @author Eric Dalquist
 * @version $Revision: 45528 $
 */
public class ContainerInfoCollector implements IPortalDataCollector, ServletContextAware {
    private ServletContext servletContext;
    
    /* (non-Javadoc)
     * @see org.springframework.web.context.ServletContextAware#setServletContext(javax.servlet.ServletContext)
     */
    public void setServletContext(ServletContext servletContext) {
        this.servletContext = servletContext;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getData()
     */
    public Map<String, String> getData() {
        final Map<String, String> data = new LinkedHashMap<String, String>();
        
        data.put("serverInfo", this.servletContext.getServerInfo());
        data.put("majorVersion", Integer.toString(this.servletContext.getMajorVersion()));
        data.put("minorVersion", Integer.toString(this.servletContext.getMinorVersion()));
        
        return data;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.portlets.registerportal.IPortalDataCollector#getKey()
     */
    public String getKey() {
        return "ServletContainer";
    }
}
