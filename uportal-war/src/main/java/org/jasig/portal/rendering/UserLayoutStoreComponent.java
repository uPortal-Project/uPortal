/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.rendering;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.cache.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Gets the {@link IUserLayoutManager} for the current request and exposes the layout XML
 * via an {@link XMLEventReader} 
 * 
 * @author Eric Dalquist
 */
public class UserLayoutStoreComponent implements StAXPipelineComponent {
    private IUserInstanceManager userInstanceManager;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final IUserLayoutManager userLayoutManager = this.getUserLayoutManager(request);
        final String cacheKey = userLayoutManager.getCacheKey();
        return CacheKey.build(this.getClass().getName(), cacheKey);
    }

    @Override
    public PipelineEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {

        final long timestamp = System.currentTimeMillis();
        logger.debug("STARTING user layout fetch for user '{}' #milestone", request.getRemoteUser());

        final IUserLayoutManager userLayoutManager = getUserLayoutManager(request);

        final XMLEventReader userLayoutReader = userLayoutManager.getUserLayoutReader();

        logger.debug("FINISHED user layout fetch for user '{}' in {}ms #milestone", request.getRemoteUser(), Long.toString(System.currentTimeMillis() - timestamp));

        return new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(userLayoutReader);
    }

    /**
     * Get the {@link IUserLayoutManager} for the user making the request
     */
    protected IUserLayoutManager getUserLayoutManager(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        return preferencesManager.getUserLayoutManager();
    }
}
