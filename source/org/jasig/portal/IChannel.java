/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal;

import java.util.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;
import org.xml.sax.DocumentHandler;

/**
 * An interface presented by a channel to a portal.
 * @author Peter Kharchenko
 * @version $Revision$
 */
public interface IChannel
{
  // following section allows portal to pass parameter/settings information to the channel

  /**
   * Passes ChannelStaticData to the channel.
   * This is done during channel instantiation time.
   * see org.jasig.portal.StaticData
   * @param sd channel static data
   * @see ChannelStaticData
   */
  public void setStaticData (ChannelStaticData sd);

  /**
   * Passes ChannelRuntimeData to the channel.
   * This function is called prior to the renderXML() call.
   * @param rd channel runtime data
   * @see ChannelRuntimeData
   */
  public void setRuntimeData (ChannelRuntimeData rd);

  /**
   * Passes an outside event to a channel.
   * Events should normally come from the LayoutBean.
   * @param ev LayoutEvent object
   * @see LayoutEvent
   */
  public void receiveEvent (LayoutEvent ev);

  // following section allows channel to pass parameter/settings information to the portal

  /**
   * Acquires ChannelSubscriptionProperties from the channel.
   * This function should be called at the Publishing/Subscription time.
   * @see ChannelSubscriptionProperties
   */
  public ChannelSubscriptionProperties getSubscriptionProperties ();

  /**
   * Acquires ChannelRuntimeProperites from the channel.
   * This function may be called by the portal framework throughout the session.
   * @see ChannelRuntimeProperties
   */
  public ChannelRuntimeProperties getRuntimeProperties ();

  // rendering Layer

  /**
   * Ask channel to render its content.
   * @param out the SAX DocumentHandler to output content to
   */
  public void renderXML (DocumentHandler out) throws Exception;
}
