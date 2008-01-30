/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.jndi;

import javax.servlet.http.HttpSession;

import org.jasig.portal.PortalException;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;
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
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final IJndiManager jndiManager = (IJndiManager) applicationContext.getBean("jndiManager", IJndiManager.class);
        jndiManager.initializeSessionContext(session, userId, layoutId, userLayout);
    }
}
