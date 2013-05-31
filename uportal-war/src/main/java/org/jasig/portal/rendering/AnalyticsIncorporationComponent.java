package org.jasig.portal.rendering;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.jasig.portal.character.stream.CharacterEventReader;
import org.jasig.portal.character.stream.FilteringCharacterEventReader;
import org.jasig.portal.character.stream.events.CharacterDataEventImpl;
import org.jasig.portal.character.stream.events.CharacterEvent;
import org.jasig.portal.events.PortalEvent;
import org.jasig.portal.events.PortletRenderExecutionEvent;
import org.jasig.portal.events.RequestScopedEventsTracker;
import org.jasig.portal.events.aggr.tabs.AggregatedTabLookupDao;
import org.jasig.portal.events.aggr.tabs.AggregatedTabMapping;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.jasig.portal.utils.cache.CacheKey;
import org.springframework.beans.factory.annotation.Autowired;

import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;

public class AnalyticsIncorporationComponent extends CharacterPipelineComponentWrapper {
    private ObjectMapper mapper;
    private AggregatedTabLookupDao aggregatedTabLookupDao;
    private IUrlSyntaxProvider urlSyntaxProvider;
    private RequestScopedEventsTracker requestScopedEventsTracker;

    @Autowired
    public void setMapper(ObjectMapper mapper) {
        this.mapper = mapper;
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
                    
                    //Filter to include just portlet render events
                    final Collection<PortalEvent> renderEventsSet = Collections2.filter(portalEvents, new Predicate<PortalEvent>() {
                        public boolean apply(PortalEvent e) {
                            return e instanceof PortletRenderExecutionEvent;
                        }
                    });
                    
                    String data = "[]";
                    try {
                        data = mapper.writeValueAsString(renderEventsSet);
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
                    
                    return CharacterDataEventImpl.create(data);
                }
                case PAGE_ANALYTICS_DATA: {
                    final Map<String, Object> pageData = new HashMap<String, Object>();
                    pageData.put("executionTimeNano", System.nanoTime() - startTime);
                    
                    final IPortalRequestInfo portalRequestInfo = urlSyntaxProvider.getPortalRequestInfo(request);
                    final String targetedLayoutNodeId = portalRequestInfo.getTargetedLayoutNodeId();
                    if (targetedLayoutNodeId != null) {
                        final AggregatedTabMapping mappedTabForLayoutId = aggregatedTabLookupDao.getMappedTabForLayoutId(targetedLayoutNodeId);
                        pageData.put("tab", mappedTabForLayoutId);
                    }

                    String data = "{}";
                    try {
                        data = mapper.writeValueAsString(pageData);
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
                    
                    return CharacterDataEventImpl.create(data);
                }
                default: {
                    return event;
                }
            }
        }
    }
}
