/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.portlet;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.PortalControlStructures;
import org.jasig.portal.PortalEvent;

/**
 * Represents the channel's state including the static data, runtime data,
 * portal event, portal control structures and a ChannelData object. 
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class ChannelState {
    private ChannelStaticData staticData = null;
    private ChannelRuntimeData runtimeData = null;
    private PortalEvent portalEvent = null;
    private PortalControlStructures pcs = null;
    private ChannelData channelData = new ChannelData();

    public ChannelStaticData getStaticData() { return this.staticData; }
    public ChannelRuntimeData getRuntimeData() { return this.runtimeData; }
    public PortalControlStructures getPortalControlStructures() { return this.pcs; }
    public PortalEvent getPortalEvent() { return this.portalEvent; }
    public ChannelData getChannelData() { return this.channelData; }

    public void setStaticData(ChannelStaticData sd) { this.staticData = sd; }
    public void setRuntimeData(ChannelRuntimeData rd) { this.runtimeData = rd; }
    public void setPortalControlStructures(PortalControlStructures pcs) { this.pcs = pcs; }
    public void setPortalEvent(PortalEvent ev) { this.portalEvent = ev; }
    public void setChannelData(ChannelData cd) { this.channelData = cd; }
}
