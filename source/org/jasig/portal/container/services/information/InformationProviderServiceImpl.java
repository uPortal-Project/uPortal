/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services.information;

import java.util.Properties;
import java.util.Vector;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.services.information.DynamicInformationProvider;
import org.apache.pluto.services.information.InformationProviderService;
import org.apache.pluto.services.information.StaticInformationProvider;
import org.jasig.portal.container.services.PortletContainerService;

/**
 * Implementation of Apache Pluto InformationProviderService.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class InformationProviderServiceImpl implements PortletContainerService, InformationProviderService {
    
    private ServletConfig servletConfig;
    private Properties properties;
    private DynamicInformationProvider provider;
	private static StaticInformationProviderImpl staticInfoProvider;
	private static int MAX_HASH_CODE_NUMBER = 10;
	private Vector hashCodes;
    
    private static final String dynamicInformationProviderRequestParameterName = "org.apache.pluto.services.information.DynamicInformationProvider";

    // PortletContainerService methods
    
    public void init(ServletConfig servletConfig, Properties properties) throws Exception {
        this.servletConfig = servletConfig;
        this.properties = properties;
        hashCodes = new Vector();
        if ( staticInfoProvider == null ) {
		 staticInfoProvider = new StaticInformationProviderImpl();
         staticInfoProvider.init(servletConfig, properties);
        } 
    }
    
    public void destroy() throws Exception {
        properties = null;
        servletConfig = null;
		staticInfoProvider = null;
		hashCodes = null;
    }    
    
    // InformationProviderService methods
    
    public StaticInformationProvider getStaticProvider() {
	   return staticInfoProvider;
    }

    public synchronized DynamicInformationProvider getDynamicProvider(HttpServletRequest request) {
      String hashCode = Integer.toString(request.hashCode());	
      if ( !hashCodes.contains(hashCode) ) {
      	if ( hashCodes.size() >= MAX_HASH_CODE_NUMBER )
      	  hashCodes.removeAllElements();	
      	hashCodes.add(hashCode);
        provider = new DynamicInformationProviderImpl(request);
      }  
        return provider;
    }

}
