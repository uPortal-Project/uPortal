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
 * IChannelTitle is an interface for conveying the title of a channel.
 * <p>Its current use (as of uPortal 2.5.1) is for marking a {@link org.jasig.portal.ChannelRuntimeProperties}
 * as conveying a dynamic channel title.  That is, an {@link org.jasig.portal.IChannel} returning
 * a {@link org.jasig.portal.ChannelRuntimeProperties} that implements IChannelTitle communicates
 * to the framework a desired title for the channel, overriding any title
 * declared at the time of channel publication.</p>
 *
 * <p>Dynamic channel title capability is implemented in terms of this interface
 * rather than in terms of detection of some particular {@link org.jasig.portal.ChannelRuntimeProperties}
 * subclass so that any existing or new {@link org.jasig.portal.ChannelRuntimeProperties} subclassess
 * can be made dynamically titled.  Recommendation for channel implementors: use
 * {@link TitledChannelRuntimeProperties} rather than writing a new
 * {@link org.jasig.portal.ChannelRuntimeProperties} subclass
 * implementing this interface, where possible.</p>
 * </p>
 * @since uPortal 2.5.1
 * @version $Revision$ $Date$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IChannelTitle {

    /**
     * Get the desired channel title.
     * Returning <code>null</code> indicates that the channel is not specifying
     * a dynamic title and will leave it up to "the uPortal framework" to
     * provide a title for the channel.
     * <p>Currently, the fallback behavior is to
     * behave as if the channel hadn't provided an IDynamicChannelTitle at all
     * and use the title specified at channel publication.</p>
     * @return desired dynamic channel title, or null if no dynamic title.
     */
    public String getChannelTitle();

}
