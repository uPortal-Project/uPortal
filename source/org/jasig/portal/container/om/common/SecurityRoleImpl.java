/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.common;

import java.io.Serializable;

import org.apache.pluto.om.common.SecurityRole;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class SecurityRoleImpl implements SecurityRole, Serializable {

    private String description;
    private String roleName;
    
    // SecurityRole methods
    
    public String getDescription() {
        return description;
    }

    public String getRoleName() {
        return roleName;
    }
    
    // Additional methods
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

}
