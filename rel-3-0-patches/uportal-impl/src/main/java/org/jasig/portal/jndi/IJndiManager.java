/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.jndi;

import javax.servlet.http.HttpSession;

import org.springframework.jndi.JndiTemplate;
import org.w3c.dom.Document;

/**
 * JNDIManager.
 *
 * uPortal's JNDI tree has the following basic structure:
 * <tt>
 * root context
 *    |
 *    +--services--*[service name]*...
 *    |
 *    +--users--*[userID]*
 *    |             |
 *    |             +--layouts--*[layoutId]*
 *    +             |               |
 * sessions         |               +--channel-ids
 *    |             |               |      |
 * *[sessionId]*    |               |      +--*[fname]*--[chanId]
 *                  |               |
 *                  |               +--sessions--*[sessionId]*
 *                  |
 *                  |
 *                  +--sessions--*[sessionId]*
 *                                    |
 *                                    +--channel-obj--*[chanId]*...
 *                                    |
 *                                    +--[layoutId]
 * </tt>
 * Notation:
 *  [something] referes to a value of something
 *  *[something]* refers to a set of values
 *  ... refers to a subcontext
 *
 *
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @author Peter Kharchenko, pkharchenko@interactivebusiness.com
 * @version $Revision: 42472 $
 */
public interface IJndiManager {
    /**
     * @return The Spring JndiTemplate used for accessing the portal JNDI context.
     * @see org.springframework.jndi.JndiAccessor#getJndiTemplate()
     */
    public JndiTemplate getJndiTemplate();

    /**
     * Create and bind objects to the portal context for a new user's session
     * 
     * @param session The new session
     * @param userId The user ID the session is for
     * @param layoutId The layout ID for the user and session
     * @param userLayout The layout document for the user and session
     */
    public void initializeSessionContext(HttpSession session, String userId, String layoutId, Document userLayout);

    /**
     * Unbind objects related to the specific session.
     * 
     * @param session
     */
    public void destroySessionContext(HttpSession session);
}
