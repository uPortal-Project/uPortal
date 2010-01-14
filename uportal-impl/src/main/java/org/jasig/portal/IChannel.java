/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal;

import org.xml.sax.ContentHandler;

/**
 * An interface presented by a channel to a portal.
 * A channel is a stateful entity. The main part of the channel lifecycle is 
 * the rendering cycle. The rendering cycle starts with an invokation of the {@link #setRuntimeData} method
 * for updating channel state, and is eventually followed by a call to the {@link #renderXML} method that 
 * retrieves the current state of the channel. These are the main two methods of the interface.
 * 
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IChannel
{
  /**
   * Passes ChannelStaticData to the channel.
   * This is done during channel instantiation time.
   * see org.jasig.portal.ChannelStaticData
   * @param sd channel static data
   * @see ChannelStaticData
   */
    public void setStaticData(ChannelStaticData sd) throws PortalException;

  /**
   * Passes ChannelRuntimeData to the channel.
   * This function is called prior to the renderXML() call.
   * @param rd channel runtime data
   * @see ChannelRuntimeData
   */
    public void setRuntimeData(ChannelRuntimeData rd) throws PortalException;

  /**
   * Passes an outside event to a channel.
   * Events should normally come from the LayoutBean.
   * @param ev PortalEvent object
   * @see PortalEvent
   */
  public void receiveEvent(PortalEvent ev);

  /**
   * Acquires ChannelRuntimeProperites from the channel.
   * This function may be called by the portal framework throughout the session.
   * @see ChannelRuntimeProperties
   */
  public ChannelRuntimeProperties getRuntimeProperties();

  /**
   * Ask channel to render its content.
   * @param out the SAX ContentHandler to output content to
   */
   public void renderXML(ContentHandler out) throws PortalException;
}
