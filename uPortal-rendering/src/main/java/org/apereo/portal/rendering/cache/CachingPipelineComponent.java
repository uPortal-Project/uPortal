/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.rendering.cache;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apereo.portal.rendering.PipelineComponentWrapper;
import org.apereo.portal.rendering.PipelineEventReader;
import org.apereo.portal.rendering.PipelineEventReaderImpl;
import org.apereo.portal.utils.cache.CacheKey;
import org.jasig.resourceserver.aggr.om.Included;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Component that caches events from a wrapped component
 *
 */
public abstract class CachingPipelineComponent<R, E> extends PipelineComponentWrapper<R, E>
        implements BeanNameAware {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private ResourcesElementsProvider resourcesElementsProvider;
    private Ehcache cache;
    private String beanName;

    @Autowired
    public void setResourcesElementsProvider(ResourcesElementsProvider resourcesElementsProvider) {
        this.resourcesElementsProvider = resourcesElementsProvider;
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
        return this.wrappedComponent.getCacheKey(request, response);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.rendering.PipelineComponent#getEventReader(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @SuppressWarnings("unchecked")
    @Override
    public final PipelineEventReader<R, E> getEventReader(
            HttpServletRequest request, HttpServletResponse response) {
        if (Included.PLAIN == this.resourcesElementsProvider.getDefaultIncludedType()) {
            this.logger.trace(
                    "{} - Resoure Aggregation Disabled, ignoring event cache and returning parent event reader directly",
                    this.beanName);
            return this.wrappedComponent.getEventReader(request, response);
        }

        //Get the key for this request from the target component and see if there is a cache entry
        final CacheKey cacheKey = this.wrappedComponent.getCacheKey(request, response);
        Element element = this.cache.get(cacheKey);
        CachedEventReader<E> cachedEventReader = null;
        if (element != null) {
            cachedEventReader = (CachedEventReader<E>) element.getObjectValue();
        }

        //If there was a cached reader return it immediately
        if (cachedEventReader == null) {
            //No cached data for key, call target component to get events and an updated cache key
            logger.debug(
                    "{} - No cached events found for key {}, calling parent",
                    this.beanName,
                    cacheKey);
            final PipelineEventReader<R, E> pipelineEventReader =
                    this.wrappedComponent.getEventReader(request, response);

            //Copy the events from the reader into a buffer to be cached
            final List<E> eventCache = new LinkedList<E>();
            for (final E event : pipelineEventReader) {
                //TODO add de-duplication logic here
                eventCache.add(event);
            }

            final Map<String, String> outputProperties = pipelineEventReader.getOutputProperties();
            cachedEventReader =
                    new CachedEventReader<E>(
                            eventCache, new LinkedHashMap<String, String>(outputProperties));

            //Cache the buffer
            element = new Element(cacheKey, cachedEventReader);
            this.cache.put(element);
            logger.debug(
                    "{} - Cached {} events for key {}", this.beanName, eventCache.size(), cacheKey);
        } else {
            logger.debug("{} - Found cached events for key {}", this.beanName, cacheKey);
        }

        final List<E> eventCache = cachedEventReader.getEventCache();
        final Map<String, String> outputProperties = cachedEventReader.getOutputProperties();

        final R eventReader = this.createEventReader(eventCache.listIterator());
        return new PipelineEventReaderImpl<R, E>(eventReader, outputProperties);
    }

    //Ugly!!! Needed because XMLEventReader implements Iterator but does not parameterize it
    protected abstract R createEventReader(ListIterator<E> eventCache);
}
