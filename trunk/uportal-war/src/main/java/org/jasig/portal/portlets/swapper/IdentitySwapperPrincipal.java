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

package org.jasig.portal.portlets.swapper;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.IPrincipal;

/**
 * Implements an immutable IPrincipal for use with the identity swapper
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class IdentitySwapperPrincipal implements IPrincipal {
    private static final long serialVersionUID = 1L;

    private final IPerson person;

    public IdentitySwapperPrincipal(IPerson person) {
        this.person = person;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.IPrincipal#getFullName()
     */
    public String getFullName() {
        return this.person.getFullName();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.IPrincipal#getGlobalUID()
     */
    public String getGlobalUID() {
        return this.person.getName();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.IPrincipal#getUID()
     */
    public String getUID() {
        return this.person.getName();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.security.IPrincipal#setUID(java.lang.String)
     */
    public void setUID(String UID) {
        throw new UnsupportedOperationException("UID is fixed for a swapped user.");
    }

}
