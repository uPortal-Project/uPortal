/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.common;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Iterator;

import org.apache.pluto.om.common.SecurityRoleRef;
import org.apache.pluto.om.common.SecurityRoleRefSet;
import org.apache.pluto.om.common.SecurityRoleRefSetCtrl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class SecurityRoleRefSetImpl extends HashSet implements SecurityRoleRefSet, SecurityRoleRefSetCtrl, Serializable {

    // SecurityRoleRefSet methods
    
    public SecurityRoleRef get(String name) {
        SecurityRoleRef securityRoleRef = null;
        Iterator iterator = iterator();
        while (iterator.hasNext()) {
            SecurityRoleRef securityRoleRefCandidate = (SecurityRoleRef)iterator.next();
            if (securityRoleRefCandidate.getRoleName().equals(name)) {
                securityRoleRef = securityRoleRefCandidate;
            }
        }
        return securityRoleRef;
    }
    
    // SecurityRoleRefSetCtrl methods

    public SecurityRoleRef add(SecurityRoleRef securityRoleRef) {
        super.add(securityRoleRef);
        return securityRoleRef;
    }

    public void remove(SecurityRoleRef securityRoleRef) {
        super.remove(securityRoleRef);
    }

    public SecurityRoleRef remove(String name) {
        SecurityRoleRef securityRoleRef = this.get(name);
        return securityRoleRef;
    }

}
