/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.servlet;

import java.io.Serializable;

/**
 * Data structure to support WebApplicationDefinition for
 * marshalling and unmarshalling of web.xml.
 * Not needed by the Pluto container.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class LoginConfigImpl implements Serializable {

    private String authMethod;
    private String realmName;
    private FormLoginConfigImpl formLoginConfig;
    
    public String getAuthMethod() {
        return authMethod;
    }

    public String getRealmName() {
        return realmName;
    }
    
    public FormLoginConfigImpl getFormLoginConfig() {
        return formLoginConfig;
    }

    public void setAuthMethod(String authMethod) {
        this.authMethod = authMethod;
    }
    
    public void setRealmName(String realmName) {
        this.realmName = realmName;
    }
    
    public void setFormLoginConfig(FormLoginConfigImpl formLoginConfig) {
        this.formLoginConfig = formLoginConfig;
    }

}
