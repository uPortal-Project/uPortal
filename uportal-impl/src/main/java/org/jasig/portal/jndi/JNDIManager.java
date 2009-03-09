/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.jndi;

import javax.servlet.http.HttpSession;

import org.jasig.portal.PortalException;
import org.jasig.portal.spring.locator.JndiManagerLocator;
import org.w3c.dom.Document;

/**
 * @deprecated Use {@link IJndiManager} from the Spring Application Context instead.
 */
@Deprecated
public class JNDIManager {
    /**
     * Empty constructor.
     */
    public JNDIManager() {
    }

    /**
     * Initializes root context node
     * @see JndiManagerImpl#initializePortalContext()
     */
    public static void initializePortalContext() throws PortalException {
        //NO-OP this is done in the spring context now.
    }

    /**
     * @see IJndiManager#initializeSessionContext(HttpSession, String, String, Document)
     */
    public static void initializeSessionContext(HttpSession session, String userId, String layoutId, Document userLayout) throws PortalException {
        final IJndiManager jndiManager = JndiManagerLocator.getJndiManager();
        jndiManager.initializeSessionContext(session, userId, layoutId, userLayout);
    }
}
