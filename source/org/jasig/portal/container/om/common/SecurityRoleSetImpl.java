/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.container.om.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.pluto.om.common.SecurityRole;
import org.apache.pluto.om.common.SecurityRoleSet;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class SecurityRoleSetImpl extends HashSet implements SecurityRoleSet, Serializable {

    // SecurityRoleSet methods
    
    public SecurityRole get(String name) {
        SecurityRole securityRole = null;
        Iterator iterator = this.iterator();
        while (iterator.hasNext()) {
            SecurityRole securityRoleCandidate = (SecurityRole)iterator.next();
            if (securityRoleCandidate.getRoleName().equals(name)) {
                securityRole = securityRoleCandidate;
            }
        }
        return securityRole;
    }
    
    // Additional methods
    
    public void add(SecurityRole securityRole) {
        this.add(securityRole);
    }

}
