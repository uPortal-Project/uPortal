/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.portlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.om.portlet.PortletApplicationDefinitionList;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletApplicationDefinitionListImpl implements PortletApplicationDefinitionList, Serializable {

    private Map portletApplicationDefinitions = null;

    public PortletApplicationDefinitionListImpl() {
        portletApplicationDefinitions = new HashMap();
    }

    public Iterator iterator() {
        return portletApplicationDefinitions.values().iterator();
    }

    public PortletApplicationDefinition get(ObjectID id) {
        return (PortletApplicationDefinition)portletApplicationDefinitions.get(id.toString());
    }
    
    // Additional methods
    
    public void add(String id, PortletApplicationDefinition definition) {
        portletApplicationDefinitions.put(id, definition);
    }

}
