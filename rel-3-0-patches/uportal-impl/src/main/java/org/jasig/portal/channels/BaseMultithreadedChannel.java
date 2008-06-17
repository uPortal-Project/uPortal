/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
 * Channels that extend BaseMultithreadedChannel typically only need to override the renderXML method
 * and should not contain any non-static member variables.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public abstract class BaseMultithreadedChannel implements IMultithreadedChannel {
  protected static Map channelStateMap;

  /**
   * A Commons Logging log instance which will log as the runtime class extending
   * this BaseChannel.  Channels extending BaseChannel can use this Log instance
   * rather than instantiating their own.
   */
  protected Log log = LogFactory.getLog(getClass());
  
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
   * @param uid a unique ID used to identify the state of the channel
   * @return channel runtime properties
   */
  public ChannelRuntimeProperties getRuntimeProperties (String uid) {
    return new ChannelRuntimeProperties();
  }

  /**
   * React to portal events.
   * Removes channel state from the channel state map when the session expires.
   * @param ev a portal event
   * @param uid a unique ID used to identify the state of the channel
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
   * @param sd the channel static data
   * @param uid a unique ID used to identify the state of the channel
   * @throws org.jasig.portal.PortalException
   */
  public void setStaticData (ChannelStaticData sd, String uid) throws PortalException {
    ChannelState channelState = new ChannelState();
    channelState.setStaticData(sd);
    channelStateMap.put(uid, channelState);
  }

  /**
   * Sets the channel runtime data.
   * @param rd the channel runtime data
   * @param uid a unique ID used to identify the state of the channel
   * @throws org.jasig.portal.PortalException
   */
  public void setRuntimeData (ChannelRuntimeData rd, String uid) throws PortalException {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    channelState.setRuntimeData(rd);
  }

  /**
   * Render nothing.
   * @param out the content handler to which the channel sends SAX events
   * @param uid a unique ID used to identify the state of the channel
   * @throws org.jasig.portal.PortalException
   */
  public void renderXML (ContentHandler out, String uid) throws PortalException {}
}



