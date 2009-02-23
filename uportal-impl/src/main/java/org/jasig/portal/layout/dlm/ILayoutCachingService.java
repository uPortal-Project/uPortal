/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.layout.dlm;

import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;
import org.w3c.dom.Document;

/**
 * Base API for layout caching service. The implementation should handle appropriately expiring layouts
 * when the user logs out
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface ILayoutCachingService {

    public void cacheLayout(IPerson owner, UserProfile profile, Document layout);
    
    public void removeCachedLayout(IPerson owner, UserProfile profile);

    public Document getCachedLayout(IPerson owner, UserProfile profile);

}