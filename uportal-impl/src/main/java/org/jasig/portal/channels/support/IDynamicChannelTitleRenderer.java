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

package org.jasig.portal.channels.support;

/**
 * Interface for IChannelRenderers that support dynamic channel titles.
 * Dynamic channel titles were added in uPortal 2.5.1.  In order to maintain
 * backwards compatibility, rather than adding this method to IChannelRenderer
 * itself, it is added to this optional extension interface.  IChannelRenderers
 * implementing this extention interface can take advantage of dynamic channel
 * support in the uPortal framework (specifically, in ChannelManager).
 * IChannelRenderers not changed to implement this interface will continue to
 * behave exactly as before dynamic channel title capabilities were introduced.
 * @since uPortal 2.5.1
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IDynamicChannelTitleRenderer {

    /**
     * Get the dynamic channel title, if any, for the channel
     * that this renderer has rendered.
     * 
     * This method must not be executed concurently with outputRendering():
     * the rendering framework should either first request the channel content
     * and then requets the title, or first request the title and then request
     * the channel content, but not both concurrently.  Both this method and 
     * outputRendering() block on the channel rendering worker thread, blocking
     * until the thread completes rendering the channel, or until the thread
     * times out.
     * 
     * @return String representing the dynamic channel title, or null if no 
     * dynamic channel title.
     */
    public String getChannelTitle();
    
}
