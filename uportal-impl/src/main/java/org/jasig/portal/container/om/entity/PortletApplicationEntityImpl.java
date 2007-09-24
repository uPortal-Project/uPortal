/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.entity;
import java.io.Serializable;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletEntityList;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.jasig.portal.container.om.common.ObjectIDImpl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletApplicationEntityImpl implements PortletApplicationEntity, Serializable {

    private ObjectID objectId = null;
    private PortletEntityList portletEntities = null;
    private PortletApplicationDefinition definition = null;
    
    public ObjectID getId() {
        return objectId;
    }

    public PortletEntityList getPortletEntityList() {
        return portletEntities;
    }

    public PortletApplicationDefinition getPortletApplicationDefinition() {
        return definition;
    }

    // Additional methods
    
    public void setId(String id) {
        this.objectId = ObjectIDImpl.createFromString(id);
    }
    
    public void setPortletEntityList(PortletEntityList portletEntities) {
        this.portletEntities = portletEntities;
    }
    
    public void setPortletApplicationDefinition(PortletApplicationDefinition definition) {
        this.definition = definition;
    }
}
