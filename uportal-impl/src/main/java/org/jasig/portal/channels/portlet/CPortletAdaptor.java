/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.channels.portlet;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.xml.sax.ContentHandler;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class CPortletAdaptor implements IPortletAdaptor {
    private PortalControlStructures portalControlStructures;
    private ChannelRuntimeData channelRuntimeData;

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#getRuntimeProperties()
     */
    public ChannelRuntimeProperties getRuntimeProperties() {
        // TODO Auto-generated method stub
        return null;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#receiveEvent(org.jasig.portal.PortalEvent)
     */
    public void receiveEvent(PortalEvent ev) {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#renderXML(org.xml.sax.ContentHandler)
     */
    public void renderXML(ContentHandler out) throws PortalException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#setRuntimeData(org.jasig.portal.ChannelRuntimeData)
     */
    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
        this.channelRuntimeData = rd;
        
        //Attach the runtime data as an attribute on the request so it is accessible to other portlet rendering related classes  
        final HttpServletRequest httpServletRequest = this.portalControlStructures.getHttpServletRequest();
        httpServletRequest.setAttribute(ATTRIBUTE_RUNTIME_DATA, this.channelRuntimeData);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannel#setStaticData(org.jasig.portal.ChannelStaticData)
     */
    public void setStaticData(ChannelStaticData sd) throws PortalException {
        // TODO Auto-generated method stub

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IPrivileged#setPortalControlStructures(org.jasig.portal.PortalControlStructures)
     */
    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException {
        this.portalControlStructures = pcs;
    }
}
