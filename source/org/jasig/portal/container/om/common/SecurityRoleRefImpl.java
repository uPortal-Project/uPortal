/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.common;

import java.io.Serializable;
import java.util.Locale;

import org.apache.pluto.om.common.Description;
import org.apache.pluto.om.common.DescriptionSet;
import org.apache.pluto.om.common.SecurityRoleRef;
import org.apache.pluto.om.common.SecurityRoleRefCtrl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class SecurityRoleRefImpl implements SecurityRoleRef, SecurityRoleRefCtrl, Serializable {

    private String roleName;
    private String roleLink;
    private DescriptionSet descriptions;
    
    public SecurityRoleRefImpl() {
        this.descriptions = new DescriptionSetImpl();
    }
    
    // SecurityRoleRef methods

    public String getRoleName() {
        return roleName;
    }

    public String getRoleLink() {
        return roleLink;
    }

    public Description getDescription(Locale locale) {
        return descriptions.get(locale);
    }
    
    // SecurityRoleRefCtrl methods
    
    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setRoleLink(String roleLink) {
        this.roleLink = roleLink;
    }
        
    public void setDescription(String description) {
        ((DescriptionSetImpl)descriptions).add(description, Locale.getDefault());
    }
    
    // Additional methods
    
    public void setDescriptions(DescriptionSet descriptions) {
        this.descriptions = descriptions;
    }
    
    public DescriptionSet getDescriptions() {
        return this.descriptions;
    }

}
