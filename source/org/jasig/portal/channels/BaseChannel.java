/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.xml.sax.ContentHandler;


/**
 * A base class from which channels implementing IChannel interface can be derived.
 * Use this only if you are familiar with IChannel interface.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public abstract class BaseChannel implements IChannel {
  protected ChannelStaticData staticData;
  protected ChannelRuntimeData runtimeData;

  public ChannelRuntimeProperties getRuntimeProperties() {
    return  new ChannelRuntimeProperties();
  }

  public void receiveEvent(PortalEvent ev) {}

  public void setStaticData(ChannelStaticData sd) throws PortalException {
    this.staticData = sd;
  }

  public void setRuntimeData(ChannelRuntimeData rd) throws PortalException {
    this.runtimeData = rd;
  }

  public void renderXML (ContentHandler out) throws PortalException {}
}



