/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.events.XMLEvent;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.cache.CacheKey;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Gets the {@link IUserLayoutManager} for the current request and exposes the layout XML
 * via an {@link XMLEventReader} 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class UserLayoutStoreComponent implements StAXPipelineComponent {
    private IUserInstanceManager userInstanceManager;
    
    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final IUserLayoutManager userLayoutManager = this.getUserLayoutManager(request);
        
        return this.getCacheKey(userLayoutManager);
    }

    @Override
    public CacheableEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final IUserLayoutManager userLayoutManager = getUserLayoutManager(request);
        
        final CacheKey userLayoutCacheKey = this.getCacheKey(userLayoutManager);
        
        final XMLEventReader userLayoutReader = userLayoutManager.getUserLayoutReader();

        return new CacheableEventReaderImpl<XMLEventReader, XMLEvent>(userLayoutCacheKey, userLayoutReader);
    }

    /**
     * Get the {@link IUserLayoutManager} for the user making the request
     */
    protected IUserLayoutManager getUserLayoutManager(HttpServletRequest request) {
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        return preferencesManager.getUserLayoutManager();
    }

    /**
     * Get the cache key for the specified {@link IUserLayoutManager}
     */
    protected CacheKey getCacheKey(final IUserLayoutManager userLayoutManager) {
        final String cacheKey = userLayoutManager.getCacheKey();
        return new CacheKey(cacheKey);
    }
}
