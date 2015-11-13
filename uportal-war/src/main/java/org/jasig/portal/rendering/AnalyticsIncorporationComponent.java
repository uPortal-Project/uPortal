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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.FilteringCharacterEventReader;
import org.jasig.portal.character.stream.events.CharacterDataEventImpl;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.PortletRenderExecutionEvent;
import org.jasig.portal.events.RequestScopedEventsTracker;
import org.jasig.portal.events.aggr.tabs.AggregatedTabLookupDao;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMapping;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.spring.beans.factory.ObjectMapperFactoryBean;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.jasig.portal.utils.cache.CacheKey;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.BeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

public class AnalyticsIncorporationComponent extends CharacterPipelineComponentWrapper implements InitializingBean {
    private ObjectMapper mapper;
    private ObjectWriter portletEventWriter;
    
    private AggregatedTabLookupDao aggregatedTabLookupDao;
    private IUrlSyntaxProvider urlSyntaxProvider;
    private RequestScopedEventsTracker requestScopedEventsTracker;
    

    @JsonFilter(PortletRenderExecutionEventFilterMixIn.FILTER_NAME)
    private interface PortletRenderExecutionEventFilterMixIn {
        static final String FILTER_NAME = "PortletRenderExecutionEventFilter";
    }

    //Ignored until https://github.com/FasterXML/jackson-databind/issues/245 is fixed
    //Delete the mapper related code in afterPropertiesSet once the issue is fixed
//    @Autowired
//    public void setMapper(ObjectMapper mapper) {
//        
//        //Clone the mapper so that our mixins don't break other code
//        this.mapper = mapper.copy();
//        initMapper();
//    }

    @Override
    public void afterPropertiesSet() throws Exception {
        final ObjectMapperFactoryBean omfb = new ObjectMapperFactoryBean();
        omfb.afterPropertiesSet();
        this.mapper = omfb.getObject();
        initMapper();
    }
    
    /**
     * Configure the ObjectMapper to filter out all fields on the events except
     * those that are actually needed for the analytics reporting
     */
    private void initMapper() {
        final BeanPropertyFilter filterOutAllExcept = SimpleBeanPropertyFilter.filterOutAllExcept("fname", "executionTimeNano");
        this.mapper.addMixInAnnotations(PortalEvent.class, PortletRenderExecutionEventFilterMixIn.class);
        final SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(PortletRenderExecutionEventFilterMixIn.FILTER_NAME, filterOutAllExcept);
        this.portletEventWriter = this.mapper.writer(filterProvider);
    }

    @Autowired
    public void setAggregatedTabLookupDao(AggregatedTabLookupDao aggregatedTabLookupDao) {
        this.aggregatedTabLookupDao = aggregatedTabLookupDao;
    }

    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }
    
    @Autowired
    public void setRequestScopedEventsTracker(RequestScopedEventsTracker requestScopedEventsTracker) {
        this.requestScopedEventsTracker = requestScopedEventsTracker;
    }

    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        return this.wrappedComponent.getCacheKey(request, response);
    }
    
    @Override
    public PipelineEventReader<CharacterEventReader, CharacterEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final long startTime = System.nanoTime();
        
        final PipelineEventReader<CharacterEventReader, CharacterEvent> pipelineEventReader = this.wrappedComponent.getEventReader(request, response);
        
        final CharacterEventReader eventReader = pipelineEventReader.getEventReader();

        final AnalyticsIncorporatingEventReader portletIncorporatingEventReader = new AnalyticsIncorporatingEventReader(eventReader, request, startTime);
        
        final Map<String, String> outputProperties = pipelineEventReader.getOutputProperties();
        return new PipelineEventReaderImpl<CharacterEventReader, CharacterEvent>(portletIncorporatingEventReader, outputProperties);
    }

    protected String serializePortletRenderExecutionEvents(final Set<PortalEvent> portalEvents) {
        //Filter to include just portlet render events
        final Map<String, PortletRenderExecutionEvent> renderEvents = new HashMap<String, PortletRenderExecutionEvent>();
        for (final PortalEvent portalEvent : portalEvents) {
            if (portalEvent instanceof PortletRenderExecutionEvent) {
                final PortletRenderExecutionEvent portletRenderEvent = (PortletRenderExecutionEvent) portalEvent;

                //Don't write out info for minimized portlets
                if (!WindowState.MINIMIZED.equals(portletRenderEvent.getWindowState())) {
                    final IPortletWindowId portletWindowId = portletRenderEvent.getPortletWindowId();
                    final String eventKey = portletWindowId != null ? portletWindowId.getStringId() : portletRenderEvent.getFname();
                    renderEvents.put(eventKey, portletRenderEvent);
                }
            }
        }
        
        try {
            return portletEventWriter.writeValueAsString(renderEvents);
        }
        catch (JsonParseException e) {
            logger.warn("Failed to convert this request's render events to JSON, no portlet level analytics will be included", e);
        }
        catch (JsonMappingException e) {
            logger.warn("Failed to convert this request's render events to JSON, no portlet level analytics will be included", e);
        }
        catch (IOException e) {
            logger.warn("Failed to convert this request's render events to JSON, no portlet level analytics will be included", e);
        }
        return "{}";
    }

    protected String serializePageData(HttpServletRequest request, long startTime) {
        final Map<String, Object> pageData = new HashMap<String, Object>();
        pageData.put("executionTimeNano", System.nanoTime() - startTime);
        
        final IPortalRequestInfo portalRequestInfo = urlSyntaxProvider.getPortalRequestInfo(request);
        pageData.put("urlState", portalRequestInfo.getUrlState());
        
        final String targetedLayoutNodeId = portalRequestInfo.getTargetedLayoutNodeId();
        if (targetedLayoutNodeId != null) {
            final AggregatedTabMapping mappedTabForLayoutId = aggregatedTabLookupDao.getMappedTabForLayoutId(targetedLayoutNodeId);
            pageData.put("tab", mappedTabForLayoutId);
        }
        

        try {
            return mapper.writeValueAsString(pageData);
        }
        catch (JsonParseException e) {
            logger.warn("Failed to convert this request's page data to JSON, no page level analytics will be included", e);
        }
        catch (JsonMappingException e) {
            logger.warn("Failed to convert this request's page data to JSON, no page level analytics will be included", e);
        }
        catch (IOException e) {
            logger.warn("Failed to convert this request's page data to JSON, no page level analytics will be included", e);
        }
        return "{}";
    }

    private class AnalyticsIncorporatingEventReader extends FilteringCharacterEventReader {
        private final HttpServletRequest request;
        private final long startTime;
        
        public AnalyticsIncorporatingEventReader(CharacterEventReader delegate, HttpServletRequest request, final long startTime) {
            super(delegate);
            this.request = request;
            this.startTime = startTime;
        }

        @Override
        protected CharacterEvent filterEvent(CharacterEvent event, boolean peek) {
            switch (event.getEventType()) {
                case PORTLET_ANALYTICS_DATA: {
                    //Get the set of events for the request
                    final Set<PortalEvent> portalEvents = requestScopedEventsTracker.getRequestEvents(request);

                    final String data = serializePortletRenderExecutionEvents(portalEvents);
                    
                    return CharacterDataEventImpl.create(data);
                }
                case PAGE_ANALYTICS_DATA: {
                    final String data = serializePageData(request, startTime);
                    
                    return CharacterDataEventImpl.create(data);
                }
                default: {
                    return event;
                }
            }
        }
    }
}
