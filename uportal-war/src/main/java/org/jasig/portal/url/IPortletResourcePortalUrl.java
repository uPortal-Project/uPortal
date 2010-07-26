/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.url;

import org.apache.pluto.container.ResourceURLProvider;

/**
 * Specific type of portal URL that targets a portlet. The URL can have portal parameters and portlet parameters, support
 * for setting the next WindowState and PortletMode for the portlet.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletResourcePortalUrl extends ResourceURLProvider {
    
}
