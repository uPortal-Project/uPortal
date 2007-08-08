/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.portlet;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletMode;

import org.apache.pluto.om.portlet.ContentType;
import org.apache.pluto.om.portlet.ContentTypeSet;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ContentTypeSetImpl implements ContentTypeSet, Serializable {
    
    private Map contentTypes = null;

    public ContentTypeSetImpl() {
        contentTypes = new HashMap();
    }

    public Iterator iterator() {
        return contentTypes.values().iterator();
    }

    public ContentType get(String contentType) {
        return (ContentType)contentTypes.get(contentType);
    }

    public boolean supportsPortletMode(PortletMode portletMode) {
        boolean supportsPortletMode = false;
        Iterator iter = this.iterator();
        while (iter.hasNext()) {
            ContentType contentType = (ContentType)iter.next();
            if (contentType.supportsPortletMode(portletMode)) {
                supportsPortletMode = true;
                break;
            }
        } 
        return supportsPortletMode;
    }

    // Additional methods
    
    public void add(ContentType contentType) {
        contentTypes.put(contentType.getContentType(), contentType);
    }
}
