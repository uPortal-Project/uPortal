/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.url;

import java.util.List;
import java.util.Map;

/**
 * Common base for a portal URL, provides methods to set parameters for the portal. Portal URLs generally
 * provide support for navigational changes within the portal rendered layout.
 * 
 * The portal URL implementation 'x-www-form-urlencoded' encodes all parameter names and values. Developers should not encode them.
 * 
 * The portal may prefix the attribute names internally
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IBasePortalUrl {
    /**
     * Sets a URL parameter targeted to the portal.
     * 
     * This method replaces all parameters with the given key.
     * 
     * @param name The parameter name
     * @param values The value or values for the parameter
     */
    public void setPortalParameter(String name, String... values);
    
    /**
     * Sets a portal parameter map for this URL.
     * 
     * All previously set portal parameters are cleared.
     * 
     * @param parameters Map containing parameters
     */
    public void setPortalParameters(Map<String, List<String>> parameters);
    
    /**
     * Get the current portal parameters. The Map is mutable and making changes to the Map will affect the parameters on the URL.
     * 
     * @return Map containing currently set portal parameters.
     */
    public Map<String, List<String>> getPortalParameters();
    
    /**
     * @return Generate a URL to be used in markup or as a redirect. The URL will be absolute, starting with a / or with a protocol such as http://
     */
    public String getUrlString();
}
