/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.window;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowList;
import org.apache.pluto.om.window.PortletWindowListCtrl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletWindowListImpl implements PortletWindowList, PortletWindowListCtrl, Serializable {

    Map portletWindows = null; // ID --> PortletWindow

    public PortletWindowListImpl() {
        portletWindows = new HashMap();
    }

    // PortletWindowList methods
    
    public Iterator iterator() {
        return portletWindows.values().iterator();
    }

    public PortletWindow get(ObjectID id) {
        return (PortletWindow)portletWindows.get(id.toString());
    }

    // PortletWindowListCtrl methods
    
    public void add(PortletWindow window) {
        portletWindows.put(window.getId().toString(), window);
    }

    public void remove(ObjectID id) {
        portletWindows.remove(id.toString());
    }

}
