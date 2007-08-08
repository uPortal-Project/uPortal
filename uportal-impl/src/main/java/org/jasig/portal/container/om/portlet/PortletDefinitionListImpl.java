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
import org.apache.pluto.om.portlet.PortletDefinition;
import org.apache.pluto.om.portlet.PortletDefinitionList;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletDefinitionListImpl implements PortletDefinitionList, Serializable {

    private Map portletDefinitions = null;

    public PortletDefinitionListImpl() {
        portletDefinitions = new HashMap();
    }

    public Iterator iterator() {
        return portletDefinitions.values().iterator();
    }

    public PortletDefinition get(ObjectID id) {
        return (PortletDefinition)portletDefinitions.get(id.toString());
    }
    
    // Additional methods
    
    public void add(String id, PortletDefinition definition) {
        portletDefinitions.put(id, definition);
    }

}
