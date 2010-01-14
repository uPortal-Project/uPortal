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
