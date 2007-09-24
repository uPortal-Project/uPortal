/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.entity;

import java.util.Iterator;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletApplicationEntity;
import org.apache.pluto.om.entity.PortletApplicationEntityList;
import org.apache.pluto.om.entity.PortletApplicationEntityListCtrl;
import org.jasig.portal.container.om.portlet.PortletApplicationDefinitionImpl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletApplicationEntityListImpl implements PortletApplicationEntityList, PortletApplicationEntityListCtrl {

    // PortletApplicationEntityList methods
    
    public PortletApplicationEntityListImpl() {
    }

    public Iterator iterator() {
        return null;
    }

    public PortletApplicationEntity get(ObjectID id) {
        return null;
    }

    // PortletApplicationEntityListCtrl methods
    
    public PortletApplicationEntity add(String definitionId) {
        // Create a new PortletApplicationEntity, 
        // add it to the list and return the new instance
        PortletApplicationDefinitionImpl definition = new PortletApplicationDefinitionImpl();
        definition.setId(definitionId);

        PortletApplicationEntityImpl portletApplicationEntityImpl = new PortletApplicationEntityImpl();  
        portletApplicationEntityImpl.setId(definitionId);
        portletApplicationEntityImpl.setPortletApplicationDefinition(definition);      
        return null;
        // not done yet!
    }

}
