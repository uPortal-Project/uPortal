/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;

import org.apache.pluto.om.common.DescriptionSet;

/**
 * Data structure to support WebApplicationDefinition for
 * marshalling and unmarshalling of web.xml.
 * Not needed by the Pluto container.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class AuthConstraintImpl implements Serializable {

    private DescriptionSet descriptions;
    private String[] roleNames; 
            
    public DescriptionSet getDescriptions() {
        return descriptions;
    }
        
    public String[] getRoleNames() {
        return roleNames;
    }
    
    public void setDescriptions(DescriptionSet descriptions) {
        this.descriptions = descriptions;
    }
        
    public void setRoleNames(String[] roleNames) {
        this.roleNames = roleNames;
    }

}
