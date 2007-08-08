/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.portlet;

import java.io.Serializable;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletDefinitionList;
import org.apache.pluto.om.servlet.WebApplicationDefinition;
import org.jasig.portal.container.om.common.ObjectIDImpl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletApplicationDefinitionImpl implements PortletApplicationDefinition, Serializable {

    private ObjectID objectId = null;
    private String version = null;
    private PortletDefinitionList portletDefinitionList = null;
    private UserAttributeListImpl userAttributes = null;
    private WebApplicationDefinition webApplicationDefinition = null;

    public ObjectID getId() {
        return objectId;
    }

    public String getVersion() {
        return version;
    }

    public PortletDefinitionList getPortletDefinitionList() {
        return portletDefinitionList;
    }

    public UserAttributeListImpl getUserAttributes() {
        return userAttributes;
    }

    public WebApplicationDefinition getWebApplicationDefinition() {
        return webApplicationDefinition;
    }

    // Additional methods
    
    public void setId(String id) {
        this.objectId = ObjectIDImpl.createFromString(id);
    }

    public void setPortletDefinitionList(PortletDefinitionList portletDefinitions) {
        this.portletDefinitionList = portletDefinitions;
    }
    
    public void setUserAttributes(UserAttributeListImpl userAttributes) {
        this.userAttributes = userAttributes;
    }    

    public void setVersion(String version) {
        this.version = version;
    }

    public void setWebApplicationDefinition(WebApplicationDefinition definition) {
        this.webApplicationDefinition = definition;
    }

}
