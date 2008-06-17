/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IChannel;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.xml.sax.ContentHandler;

/**
 * A channel which exists to throw a deeply nested exception at render time,
 * in order to give CError a chance to demonstrate its presentation of nested
 * throwables.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public final class CThrower implements IChannel
{
    private final Log log = LogFactory.getLog(getClass());

  
  /** 
   * Do-nothing constructor
   */
  public CThrower () {
      this.log.trace("CThrower()");
      // nothing to do
  }

  public ChannelRuntimeProperties getRuntimeProperties () {
      this.log.trace("getRuntimeProperties()");
    return new ChannelRuntimeProperties();
  }

  public void receiveEvent (PortalEvent ev)
  {
      if (this.log.isTraceEnabled())
          this.log.trace("received event [" + ev + "]");
    // no events for this channel
  }

  public void setStaticData (ChannelStaticData sd) {
      if (this.log.isTraceEnabled())
          this.log.trace("setStaticData(" + sd + ")");
   }

  public void setRuntimeData (ChannelRuntimeData rd) {
      if (this.log.isTraceEnabled())
          this.log.trace("setRuntimeData(" + rd + ")");
  }

  public void renderXML (ContentHandler out) throws PortalException {

      RuntimeException runtimeException = 
          new RuntimeException("Deepest exception is a RuntimeException.");
      PortalException portalException = 
          new PortalException("Middle exception is a PortalException.", 
                  runtimeException);
      PortalException outerException = 
          new PortalException("Outer exception is also a PortalException", 
                  portalException);
      throw outerException;
  }

}
