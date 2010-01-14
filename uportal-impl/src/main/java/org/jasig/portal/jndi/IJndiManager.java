/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
