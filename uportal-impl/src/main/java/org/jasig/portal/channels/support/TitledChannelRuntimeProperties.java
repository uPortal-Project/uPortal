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

import org.jasig.portal.ChannelRuntimeProperties;

/**
 * {@link ChannelRuntimeProperties} subclass implementing {@link IChannelTitle}.
 * This class is provided as a convenience to channel developers.  No uPortal
 * framework code should be written to expect this concrete implementation -
 * the framework should only detect whether the ChannelRuntimeProperties it
 * encounters implements {@link IChannelTitle}.
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public final class TitledChannelRuntimeProperties
	extends ChannelRuntimeProperties
	implements IChannelTitle {

    /**
     * Title of the channel.
     */
    private String channelTitle;

    /**
     * TitledChannelRuntimeProperties requires that the desired channel title
     * be provided at instantiation.  A null desiredTitle will cause
     * TitledChannelRuntimeProperties to return null on getTitle(), indicating
     * that the channel doesn't have a desired title.
     * @param desiredTitle desired channel title, or null if no preference
     */
    public TitledChannelRuntimeProperties(String desiredTitle) {
        this.channelTitle = desiredTitle;
    }

    public String getChannelTitle() {
        if (log.isTraceEnabled()) {
            log.trace("TitledChannelRuntimeProperties: getting title [" + this.channelTitle + "]");
        }
        return this.channelTitle;
    }

}