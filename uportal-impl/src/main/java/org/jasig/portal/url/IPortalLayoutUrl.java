/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.url;




/**
 * Specific type of portal URL that targets a portlet. The URL can have portal parameters and portlet parameters, support
 * for setting the next WindowState and PortletMode for the portlet.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortalLayoutUrl extends IBasePortalUrl {
    /**
     * @param renderInNormal If the URL will result in rendering in {@link UrlState#NORMAL}
     */
    public void setRenderInNormal(boolean renderInNormal);
    
    /**
     * @return Returns true if the URL will render in {@link UrlState#NORMAL}
     */
    public boolean isRenderInNormal();
}
