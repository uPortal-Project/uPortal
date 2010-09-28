/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering.cache;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.jasig.portal.rendering.PipelineComponent;
import org.jasig.portal.rendering.PipelineEventReader;
import org.jasig.portal.rendering.PipelineEventReaderImpl;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.resource.aggr.om.Included;
import org.jasig.resource.aggr.util.ResourcesElementsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * component that can cache character pipeline events
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class CachingPipelineComponent<R, E> implements PipelineComponent<R, E>, BeanNameAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private ResourcesElementsProvider resourcesElementsProvider;
    private PipelineComponent<R, E> parentComponent;
    private Ehcache cache;
    private String beanName;
    
    @Autowired
    public void setResourcesElementsProvider(ResourcesElementsProvider resourcesElementsProvider) {
        this.resourcesElementsProvider = resourcesElementsProvider;
    }

    public final void setParentComponent(PipelineComponent<R, E> parentComponent) {
        this.parentComponent = parentComponent;
    }

    public final void setCache(Ehcache cache) {
        this.cache = cache;
    }
    
    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public final CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        return this.parentComponent.getCacheKey(request, response);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.PipelineComponent#getEventReader(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final PipelineEventReader<R, E> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        if (Included.PLAIN == this.resourcesElementsProvider.getDefaultIncludedType()) {
            this.logger.trace("{} - Resoure Aggregation Disabled, ignoring event cache and returning parent event reader directly", this.beanName);
            return this.parentComponent.getEventReader(request, response);
        }
        
        //Get the key for this request from the target component and see if there is a cache entry
        final CacheKey cacheKey = this.parentComponent.getCacheKey(request, response);
        Element element = this.cache.get(cacheKey);
        List<E> eventCache = null;
        if (element != null) {
            eventCache = (List<E>)element.getObjectValue();
        }
        
        //No cached data for key, call target component to get events and an updated cache key
        if (eventCache == null) {
            logger.debug("{} - No cached events found for key {}, calling parent", this.beanName, cacheKey);
            final PipelineEventReader<R, E> pipelineEventReader = this.parentComponent.getEventReader(request, response);
    
            //Copy the events from the reader into a buffer to be cached
            eventCache = new LinkedList<E>();
            for (final E event : pipelineEventReader) {
                eventCache.add(event);
            }
    
            //Cache the buffer
            element = new Element(cacheKey, Collections.unmodifiableList(eventCache));
            this.cache.put(element);
            logger.debug("{} - Cached {} events for key {}", new Object[] {this.beanName, eventCache.size(), cacheKey});
        }
        else {
            logger.debug("{} - Founed {} cached events for key {}", new Object[] {this.beanName, eventCache.size(), cacheKey});
        }
        
        //Ugly!!! Needed because XMLEventReader implements Iterator but does not parameterize it
        final R eventReader = this.createEventReader(eventCache.listIterator());
        
        return new PipelineEventReaderImpl<R, E>(eventReader);
    }
    
    protected abstract R createEventReader(ListIterator<E> eventCache);
}
