/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.channels.portlet;

import java.util.Hashtable;

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
public class ChannelState extends Hashtable {
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
