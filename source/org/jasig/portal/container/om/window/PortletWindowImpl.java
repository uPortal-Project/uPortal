/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.om.window;

import java.io.Serializable;

import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.core.InternalActionResponse;
import org.apache.pluto.om.common.ObjectID;
import org.apache.pluto.om.entity.PortletEntity;
import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.om.window.PortletWindowCtrl;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.container.om.common.ObjectIDImpl;
import org.jasig.portal.container.om.entity.PortletEntityImpl;

/**
 * Implementation of Apache Pluto object model.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class PortletWindowImpl implements PortletWindow, PortletWindowCtrl, Serializable {
    
    private ObjectID objectId;
    private PortletEntity portletEntity;
    
    private ChannelRuntimeData runtimeData;
    private HttpServletRequest request;
    private InternalActionResponse internalActionResponse;

    // PortletWindow methods
    
    public ObjectID getId() {
        return objectId;
    }

    public PortletEntity getPortletEntity() {
        return portletEntity;
    }
    
    // PortletWindowCtrl methods

    public void setId(String id) {
        this.objectId = ObjectIDImpl.createFromString(id);
    }

    public void setPortletEntity(PortletEntity portletEntity) {
        this.portletEntity = portletEntity;
        ((PortletEntityImpl)portletEntity).addPortletWindow(this);
    }
    
    // Additional methods
    
    public void setChannelRuntimeData(ChannelRuntimeData runtimeData) {
        this.runtimeData = runtimeData;
    }
    
    public ChannelRuntimeData getChannelRuntimeData() {
        return this.runtimeData;
    }
    
    public void setHttpServletRequest(HttpServletRequest request) {
        this.request = request;
    }
    
    public HttpServletRequest getHttpServletRequest() {
        return this.request;
    }

    public InternalActionResponse getInternalActionResponse() {
        return internalActionResponse;
    }

    public void setInternalActionResponse(InternalActionResponse internalActionResponse) {
        this.internalActionResponse = internalActionResponse;
    }

}
