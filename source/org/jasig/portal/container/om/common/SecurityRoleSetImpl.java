/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
        super.add(securityRole);
    }

}
