/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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

package  org.jasig.portal.channels;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IMultithreadedChannel;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.xml.sax.ContentHandler;

/**
 * A base class from which channels implementing IMultithreadedChannel interface can be derived.
 * Use this only if you are familiar with the IMultithreadedChannel interface.
 * Channels that extend MultithreadedChannel typically only need to override the renderXML method
 * and should not contain any non-static member variables.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public abstract class BaseMultithreadedChannel implements IMultithreadedChannel {
  protected static Map channelStateMap;

  /**
   * The channel's state including the static data, runtime data,
   * portal event, and a channelData map which is analagous to the
   * session for a servlet - the channel can stash objects in it.
   */
  protected class ChannelState {
    private ChannelStaticData staticData = null;
    private ChannelRuntimeData runtimeData = null;
    private PortalEvent portalEvent = null;
    private Map channelData = new HashMap();

    public ChannelStaticData getStaticData() { return this.staticData; }
    public ChannelRuntimeData getRuntimeData() { return this.runtimeData; }
    public PortalEvent getPortalEvent() { return this.portalEvent; }
    public Map getChannelData() { return this.channelData; }
    public void setStaticData(ChannelStaticData sd) { this.staticData = sd; }
    public void setRuntimeData(ChannelRuntimeData rd) { this.runtimeData = rd; }
    public void setPortalEvent(PortalEvent ev) { this.portalEvent = ev; }
    public void setChannelData(Map cd) { this.channelData = cd; }
  }

  static {
    channelStateMap = Collections.synchronizedMap(new HashMap());
  }

  /**
   * Sets channel runtime properties.
   * @param uid, a unique ID used to identify the state of the channel
   * @return channel runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties (String uid) {
    return new ChannelRuntimeProperties();
  }

  /**
   * React to portal events.
   * Removes channel state from the channel state map when the session expires.
   * @param ev, a portal event
   * @param uid, a unique ID used to identify the state of the channel
   */
  public void receiveEvent (PortalEvent ev, String uid) {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    if (channelState != null) {
      channelState.setPortalEvent(ev);
      if (ev.getEventNumber() == PortalEvent.SESSION_DONE) {
        channelStateMap.remove(uid); // Clean up
      }
    }
  }

  /**
   * Sets the channel static data.
   * @param sd, the channel static data
   * @param uid, a unique ID used to identify the state of the channel
   * @throws org.jasig.portal.PortalException
   */
  public void setStaticData (ChannelStaticData sd, String uid) throws PortalException {
    ChannelState channelState = new ChannelState();
    channelState.setStaticData(sd);
    channelStateMap.put(uid, channelState);
  }

  /**
   * Sets the channel runtime data.
   * @param rd, the channel runtime data
   * @param uid, a unique ID used to identify the state of the channel
   * @throws org.jasig.portal.PortalException
   */
  public void setRuntimeData (ChannelRuntimeData rd, String uid) throws PortalException {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    channelState.setRuntimeData(rd);
  }

  /**
   * Render nothing.
   * @param out, the content handler to which the channel sends SAX events
   * @param uid, a unique ID used to identify the state of the channel
   * @throws org.jasig.portal.PortalException
   */
  public void renderXML (ContentHandler out, String uid) throws PortalException {}
}



