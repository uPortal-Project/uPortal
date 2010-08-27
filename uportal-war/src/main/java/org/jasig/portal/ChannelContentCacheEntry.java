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

import java.io.IOException;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.portlet.rendering.IPortletExecutionManager;
import org.jasig.portal.serialize.CachingSerializer;
/**
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class ChannelContentCacheEntry extends BaseChannelCacheEntry {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    public ChannelContentCacheEntry(String channelId) {
        super(channelId);
    }

    public void replayCache(CachingSerializer serializer, IPortletExecutionManager portletExecutionManager,
        HttpServletRequest req, HttpServletResponse res)
        throws PortalException {
        
        try {
            final StringWriter output = new StringWriter();
            portletExecutionManager.outputPortlet(this.getChannelId(), req, res, output);
            serializer.printRawCharacters(output.toString());
        }
        catch (IOException ioe) {
            //TODO better error handling
            this.logger.error("Failed to incorporate channel " + this.getChannelId(), ioe);
        }
    }

    public CacheType getCacheType() {
        return CacheType.CHANNEL_CONTENT;
    }
}
