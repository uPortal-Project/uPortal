/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.error;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelRuntimeProperties;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.GeneralRenderingException;
import org.jasig.portal.IChannel;
import org.jasig.portal.PortalEvent;
import org.jasig.portal.PortalException;
import org.xml.sax.ContentHandler;

/**
 * A channel which exists to demonstrate timing out on render.
 * This channel will hang for 30 seconds on rendering.  This will likely be greater 
 * than the configured channel timeout and so the channel rendering framework
 * should fall back on using CError to render an error message.  This channel
 * exists to facilitate demonstrating that fallback behavior.
 * @author andrew.petro@yale.edu
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public final class CTimeout 
    implements IChannel {
    
    protected final Log log = LogFactory.getLog(getClass());

  
  /** 
   * Do-nothing constructor
   */
  public CTimeout () {
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
      
      // TODO this channel could get timeout configuration from static or
      // runtime data
  }

  public void renderXML (ContentHandler out) throws PortalException {

      long startRendering = System.currentTimeMillis();
      
      // do something that takes a long time
      
      Object dummyLock = new Object();
      
      synchronized (dummyLock) {
          try {
              // wait 30 seconds
            dummyLock.wait(30000);
        } catch (InterruptedException e) {
            long interrupted = System.currentTimeMillis();
            
            long elapsedMillis = interrupted - startRendering;
            
            // we're a test channel, let's let the developer know we were interrupted
            throw new GeneralRenderingException("CTimeout interrupted after " + elapsedMillis + " milliseconds.");
        }
      }
      
     long returningFromRendering = System.currentTimeMillis();
     log.trace("CTimeout returning from rendering after " + (returningFromRendering - startRendering) + " milliseconds.");
  }

}
