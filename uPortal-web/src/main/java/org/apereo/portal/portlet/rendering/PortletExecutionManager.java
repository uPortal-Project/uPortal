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
package org.apereo.portal.portlet.rendering;

import com.google.common.base.Function;
import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.portlet.Event;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.commons.lang3.StringUtils;
import org.apache.pluto.container.om.portlet.ContainerRuntimeOption;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apereo.portal.events.IPortletExecutionEventFactory;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.IPortletDescriptorKey;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.portlet.rendering.worker.IPortletExecutionContext;
import org.apereo.portal.portlet.rendering.worker.IPortletExecutionInterceptor;
import org.apereo.portal.portlet.rendering.worker.IPortletExecutionWorker;
import org.apereo.portal.portlet.rendering.worker.IPortletFailureExecutionWorker;
import org.apereo.portal.portlet.rendering.worker.IPortletRenderExecutionWorker;
import org.apereo.portal.portlet.rendering.worker.IPortletWorkerFactory;
import org.apereo.portal.portlets.error.MaintenanceModeException;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.utils.ConcurrentMapUtils;
import org.apereo.portal.utils.personalize.IPersonalizer;
import org.apereo.portal.utils.web.PortalWebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jmx.export.annotation.ManagedResource;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

/**
 * Handles the asynchronous execution of portlets, handling execution errors and publishing events
 * about the execution.
 */
@ManagedResource("uPortal:section=Framework,name=PortletExecutionManager")
@Service("portletExecutionManager")
public class PortletExecutionManager extends HandlerInterceptorAdapter
        implements IPortletExecutionManager,
                IPortletExecutionInterceptor,
                PortletExecutionManagerMXBean {

    /**
     * Optional publishing parameter that makes a portlet ineligable to send or receive events.
     * Improves performance when they are not needed.
     */
    public static final String DISABLE_PORTLET_EVENTS_PARAMETER = "disablePortletEvents";

    private static final long DEBUG_TIMEOUT = TimeUnit.HOURS.toMillis(1);
    private static final String PORTLET_HEADER_RENDERING_MAP =
            PortletExecutionManager.class.getName() + ".PORTLET_HEADER_RENDERING_MAP";
    private static final String PORTLET_RENDERING_MAP =
            PortletExecutionManager.class.getName() + ".PORTLET_RENDERING_MAP";

    protected static final String SESSION_ATTRIBUTE__PORTLET_FAILURE_CAUSE_MAP =
            PortletExecutionManager.class.getName() + ".PORTLET_FAILURE_CAUSE_MAP";

    /**
     * 'javax.portlet.renderHeaders' is the name of a container runtime option a JSR-286 portlet can
     * enable to trigger header output
     */
    protected static final String PORTLET_RENDER_HEADERS_OPTION = "javax.portlet.renderHeaders";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /** Queue used to track workers that did not complete in their allotted time. */
    private final Queue<IPortletExecutionWorker<?>> hungWorkers =
            new ConcurrentLinkedQueue<IPortletExecutionWorker<?>>();

    private final ConcurrentMap<IPortletDescriptorKey, AtomicInteger> executionCount =
            ConcurrentMapUtils.makeDefaultsMap(
                    new Function<IPortletDescriptorKey, AtomicInteger>() {
                        @Override
                        public AtomicInteger apply(IPortletDescriptorKey key) {
                            return new AtomicInteger();
                        }
                    });

    private boolean ignoreTimeouts = false;
    private int extendedTimeoutExecutions = 5;
    private long extendedTimeoutMultiplier = 20;
    private int maxEventIterations = 100;
    private IPersonalizer personalizer;
    private IPersonManager personManager;
    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEventCoordinationService eventCoordinationService;
    private IPortletWorkerFactory portletWorkerFactory;
    private IPortletExecutionEventFactory portletExecutionEventFactory;

    /**
     * @param maxEventIterations The maximum number of iterations to spend dispatching events.
     *     Defaults to 100
     */
    @Override
    @Value("${org.apereo.portal.portlet.maxEventIterations:100}")
    public void setMaxEventIterations(int maxEventIterations) {
        this.maxEventIterations = maxEventIterations;
    }

    @Override
    public int getMaxEventIterations() {
        return this.maxEventIterations;
    }

    @Override
    @Value("${org.apereo.portal.portlet.ignoreTimeout}")
    public void setIgnoreTimeouts(boolean ignoreTimeouts) {
        this.ignoreTimeouts = ignoreTimeouts;
    }

    @Override
    public boolean isIgnoreTimeouts() {
        return this.ignoreTimeouts;
    }

    @Override
    @Value("${org.apereo.portal.portlet.extendedTimeoutExecutions:5}")
    public void setExtendedTimeoutExecutions(int extendedTimeoutExecutions) {
        this.extendedTimeoutExecutions = extendedTimeoutExecutions;
    }

    @Override
    public int getExtendedTimeoutExecutions() {
        return this.extendedTimeoutExecutions;
    }

    @Override
    @Value("${org.apereo.portal.portlet.extendedTimeoutMultiplier:20}")
    public void setExtendedTimeoutMultiplier(long extendedTimeoutMultiplier) {
        this.extendedTimeoutMultiplier = extendedTimeoutMultiplier;
    }

    @Override
    public long getExtendedTimeoutMultiplier() {
        return this.extendedTimeoutMultiplier;
    }

    @Override
    public Map<String, Integer> getPortletExecutionCounts() {
        final Map<String, Integer> counts = new TreeMap<String, Integer>();

        for (final Map.Entry<IPortletDescriptorKey, AtomicInteger> entry :
                this.executionCount.entrySet()) {
            final IPortletDescriptorKey key = entry.getKey();
            final AtomicInteger value = entry.getValue();
            counts.put(key.getWebAppName() + "/" + key.getPortletName(), value.get());
        }

        return counts;
    }

    @Autowired
    public void setPersonalizer(IPersonalizer personalizer) {
        this.personalizer = personalizer;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setPortletWorkerFactory(IPortletWorkerFactory portletWorkerFactory) {
        this.portletWorkerFactory = portletWorkerFactory;
    }

    @Autowired
    public void setEventCoordinationService(
            IPortletEventCoordinationService eventCoordinationService) {
        this.eventCoordinationService = eventCoordinationService;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Autowired
    public void setPortletExecutionEventFactory(
            IPortletExecutionEventFactory portletExecutionEventFactory) {
        this.portletExecutionEventFactory = portletExecutionEventFactory;
    }

    @Override
    public void afterCompletion(
            HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
            throws Exception {

        final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletHeaderRenderingMap =
                this.getPortletHeaderRenderingMap(request);
        for (final IPortletRenderExecutionWorker portletRenderExecutionWorker :
                portletHeaderRenderingMap.values()) {
            checkWorkerCompletion(request, portletRenderExecutionWorker);
        }

        final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap =
                this.getPortletRenderingMap(request);
        for (final IPortletRenderExecutionWorker portletRenderExecutionWorker :
                portletRenderingMap.values()) {
            checkWorkerCompletion(request, portletRenderExecutionWorker);
        }
    }

    /** Checks to see if a worker has been retrieved (not orphaned) and if it is complete. */
    protected void checkWorkerCompletion(
            HttpServletRequest request,
            IPortletRenderExecutionWorker portletRenderExecutionWorker) {
        if (!portletRenderExecutionWorker.isRetrieved()) {
            final IPortletWindowId portletWindowId =
                    portletRenderExecutionWorker.getPortletWindowId();
            final IPortletWindow portletWindow =
                    this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
            this.logger.warn(
                    "Portlet worker started but never retrieved for {}, worker {}."
                            + " If random portlet fnames it may be users switching tabs before page is done rendering"
                            + " (would see separate log message with java.net.SocketException on socket write)."
                            + " If repeatedly occurring with one portlet fname your theme layout xsl may not be including"
                            + " a portlet present in your layout xml files (see"
                            + " http://jasig.275507.n4.nabble.com/Portlet-worker-started-but-never-retrieved-td4580698.html)",
                    portletWindow,
                    portletRenderExecutionWorker);

            try {
                portletRenderExecutionWorker.get(0);
            } catch (Exception e) {
                // Ignore exception here, we just want to get this worker to complete
            }
        }

        if (!portletRenderExecutionWorker.isComplete()) {
            cancelWorker(request, portletRenderExecutionWorker);
        }
    }

    /** Cancel the worker and add it to the hung workers queue */
    protected void cancelWorker(
            HttpServletRequest request, IPortletExecutionWorker<?> portletExecutionWorker) {
        final IPortletWindowId portletWindowId = portletExecutionWorker.getPortletWindowId();
        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        this.logger.warn(
                "{} has not completed, adding to hung-worker cleanup queue: {}",
                portletExecutionWorker,
                portletWindow);

        portletExecutionWorker.cancel();

        this.portletExecutionEventFactory.publishPortletHungEvent(
                request, this, portletExecutionWorker);
        hungWorkers.offer(portletExecutionWorker);
    }

    @Scheduled(fixedRate = 1000)
    public void cleanupHungWorkers() {
        if (this.hungWorkers.isEmpty()) {
            return;
        }

        for (final Iterator<IPortletExecutionWorker<?>> workerItr = this.hungWorkers.iterator();
                workerItr.hasNext(); ) {
            final IPortletExecutionWorker<?> worker = workerItr.next();

            // If the worker completed remove it from queue
            if (worker.isComplete()) {
                workerItr.remove();
                this.logger.debug(
                        "{} has completed and is removed from the hung worker queue after {} cancels",
                        worker,
                        worker.getCancelCount());

                this.portletExecutionEventFactory.publishPortletHungCompleteEvent(this, worker);
            }
            // If the worker is still running cancel it
            else {
                // Log a warning about the worker once every 30 seconds or so
                final int cancelCount = worker.getCancelCount();
                if (cancelCount % 150 == 0) {
                    this.logger.warn(
                            "{} is still hung, cancel has been called {} times",
                            worker,
                            cancelCount);
                } else {
                    this.logger.debug(
                            "{} is still hung, cancel has been called {} times",
                            worker,
                            cancelCount);
                }

                worker.cancel();
            }
        }
    }

    @Override
    public void preSubmit(
            HttpServletRequest request,
            HttpServletResponse response,
            IPortletExecutionContext context) {}

    @Override
    public void preExecution(
            HttpServletRequest request,
            HttpServletResponse response,
            IPortletExecutionContext context) {}

    @Override
    public void postExecution(
            HttpServletRequest request,
            HttpServletResponse response,
            IPortletExecutionContext context,
            Exception e) {
        final IPortletWindowId portletWindowId = context.getPortletWindowId();
        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final IPortletDescriptorKey portletDescriptorKey =
                portletDefinition.getPortletDescriptorKey();

        final AtomicInteger counter = this.executionCount.get(portletDescriptorKey);
        counter.incrementAndGet();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.rendering.IPortletExecutionManager#doPortletAction(org.apereo.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doPortletAction(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        final long timeout = getPortletActionTimeout(portletWindowId, request);

        final IPortletExecutionWorker<Long> portletActionExecutionWorker =
                this.portletWorkerFactory.createActionWorker(request, response, portletWindowId);
        portletActionExecutionWorker.submit();

        try {
            portletActionExecutionWorker.get(timeout);
        } catch (Exception e) {
            // put the exception into the error map for the session
            final Map<IPortletWindowId, Exception> portletFailureMap = getPortletErrorMap(request);
            portletFailureMap.put(portletWindowId, e);
        }

        // If the worker is still running add it to the hung-workers queue
        if (!portletActionExecutionWorker.isComplete()) {
            cancelWorker(request, portletActionExecutionWorker);
        }

        // Is this portlet permitted to emit events?  (Or is it disablePortletEvents=true?)
        final IPortletWindow portletWindow =
                portletWindowRegistry.getPortletWindow(request, portletWindowId);
        IPortletDefinition portletDefinition =
                portletWindow.getPortletEntity().getPortletDefinition();
        IPortletDefinitionParameter disablePortletEvents =
                portletDefinition.getParameter(DISABLE_PORTLET_EVENTS_PARAMETER);
        if (disablePortletEvents != null && Boolean.parseBoolean(disablePortletEvents.getValue())) {
            logger.info(
                    "Ignoring portlet events for portlet '{}' because they have been disabled.",
                    portletDefinition.getFName());
        } else {
            // Proceed with events...
            final PortletEventQueue portletEventQueue =
                    this.eventCoordinationService.getPortletEventQueue(request);
            this.doPortletEvents(portletEventQueue, request, response);
        }
    }

    public void doPortletEvents(
            PortletEventQueue eventQueue,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (eventQueue.getUnresolvedEvents().isEmpty()) {
            return;
        }

        final Map<IPortletWindowId, IPortletExecutionWorker<Long>> eventWorkers =
                new LinkedHashMap<IPortletWindowId, IPortletExecutionWorker<Long>>();

        // TODO what to do if we hit the max iterations?
        int iteration = 0;
        for (; iteration < this.maxEventIterations; iteration++) {
            // Make sure all queued events have been resolved
            this.eventCoordinationService.resolvePortletEvents(request, eventQueue);

            // Create and submit an event worker for each window with a queued event
            for (final IPortletWindowId eventWindowId : eventQueue) {
                if (eventWorkers.containsKey(eventWindowId)) {
                    /*
                     * PLT.15.2.5 says that event processing per window must be serialized, if there
                     * is already a working in the map for the window ID skip it for now. we'll get back to it eventually
                     */
                    continue;
                }

                final QueuedEvent queuedEvent = eventQueue.pollEvent(eventWindowId);

                if (queuedEvent != null) {
                    final Event event = queuedEvent.getEvent();
                    final IPortletExecutionWorker<Long> portletEventExecutionWorker =
                            this.portletWorkerFactory.createEventWorker(
                                    request, response, eventWindowId, event);
                    eventWorkers.put(eventWindowId, portletEventExecutionWorker);
                    portletEventExecutionWorker.submit();
                }
            }

            // If no event workers exist we're done with event processing!
            if (eventWorkers.isEmpty()) {
                return;
            }

            // See if any of the events have completed
            int completedEventWorkers = 0;
            final Set<Entry<IPortletWindowId, IPortletExecutionWorker<Long>>> entrySet =
                    eventWorkers.entrySet();
            for (final Iterator<Entry<IPortletWindowId, IPortletExecutionWorker<Long>>>
                            eventWorkerEntryItr = entrySet.iterator();
                    eventWorkerEntryItr.hasNext(); ) {
                final Entry<IPortletWindowId, IPortletExecutionWorker<Long>> eventWorkerEntry =
                        eventWorkerEntryItr.next();

                final IPortletExecutionWorker<Long> eventWorker = eventWorkerEntry.getValue();
                if (eventWorker.isComplete()) {
                    final IPortletWindowId portletWindowId = eventWorkerEntry.getKey();
                    // TODO return number of new queued events, use to break the loop earlier
                    waitForEventWorker(request, eventQueue, eventWorker, portletWindowId);

                    eventWorkerEntryItr.remove();
                    completedEventWorkers++;
                }
            }

            /*
             * If no event workers have completed without waiting wait for the first one and then loop again
             * Not waiting for all events since each event may spawn more events and we want to start them
             * processing as soon as possible
             */
            if (completedEventWorkers == 0) {
                final Iterator<Entry<IPortletWindowId, IPortletExecutionWorker<Long>>>
                        eventWorkerEntryItr = entrySet.iterator();
                final Entry<IPortletWindowId, IPortletExecutionWorker<Long>> eventWorkerEntry =
                        eventWorkerEntryItr.next();
                eventWorkerEntryItr.remove();

                final IPortletWindowId portletWindowId = eventWorkerEntry.getKey();
                final IPortletExecutionWorker<Long> eventWorker = eventWorkerEntry.getValue();
                waitForEventWorker(request, eventQueue, eventWorker, portletWindowId);
            }
        }

        if (iteration == this.maxEventIterations) {
            this.logger.error(
                    "The Event dispatching iteration maximum of "
                            + this.maxEventIterations
                            + " was hit, consider either raising this limit or reviewing the portlets that use events to reduce the number of events spawned");
        }
    }

    protected void waitForEventWorker(
            HttpServletRequest request,
            PortletEventQueue eventQueue,
            IPortletExecutionWorker<Long> eventWorker,
            IPortletWindowId portletWindowId) {

        final long timeout = getPortletEventTimeout(portletWindowId, request);

        try {
            eventWorker.get(timeout);
        } catch (Exception e) {
            // put the exception into the error map for the session
            // TODO event error handling?
            final IPortletWindow portletWindow =
                    this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
            logger.warn(
                    portletWindow
                            + " threw an exception while executing an event. This chain of event handling will terminate.",
                    e);
        }

        // If the worker is still running add it to the hung-workers queue
        if (!eventWorker.isComplete()) {
            cancelWorker(request, eventWorker);
        }
    }

    /**
     * Only actually starts rendering the head if the portlet has the 'javax.portlet.renderHeaders'
     * container-runtime-option present and set to "true."
     *
     * @see
     *     IPortletExecutionManager#startPortletHeaderRender(org.apereo.portal.portlet.om.IPortletWindowId,
     *     javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void startPortletHeaderRender(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (doesPortletNeedHeaderWorker(portletWindowId, request)) {
            this.startPortletHeaderRenderInternal(portletWindowId, request, response);
        } else {
            this.logger.debug(
                    "ignoring startPortletHeadRender request since containerRuntimeOption is not present for portletWindowId "
                            + portletWindowId);
        }
    }

    /**
     * @param portletWindowId
     * @param request
     * @return
     */
    protected boolean doesPortletNeedHeaderWorker(
            IPortletWindowId portletWindowId, HttpServletRequest request) {
        IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        PortletDefinition portletDefinition =
                portletWindow.getPlutoPortletWindow().getPortletDefinition();
        ContainerRuntimeOption renderHeaderOption =
                portletDefinition.getContainerRuntimeOption(PORTLET_RENDER_HEADERS_OPTION);
        boolean result = false;
        if (renderHeaderOption != null) {
            result = renderHeaderOption.getValues().contains(Boolean.TRUE.toString());
        }
        logger.debug(
                "Portlet {} need render header worker: {}",
                portletDefinition.getPortletName(),
                result);
        return result;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.rendering.IPortletExecutionManager#startPortletRender(org.apereo.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void startPortletRender(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        this.startPortletRenderInternal(portletWindowId, request, response);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.rendering.IPortletExecutionManager#serveResource(org.apereo.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public void doPortletServeResource(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        final long timeout = getPortletResourceTimeout(portletWindowId, request);

        final IPortletExecutionWorker<Long> resourceWorker =
                this.portletWorkerFactory.createResourceWorker(request, response, portletWindowId);
        resourceWorker.submit();

        try {
            resourceWorker.get(timeout);
        } catch (Exception e) {
            // Log the exception but not this thread's stacktrace. The portlet worker has already
            // logged its stack trace
            this.logger.error(
                    "resource worker {} failed with exception {}", resourceWorker, e.toString());
            // render generic serveResource error
            try {
                if (!response.isCommitted()) {
                    response.sendError(
                            HttpServletResponse.SC_SERVICE_UNAVAILABLE, "resource unavailable");
                }
            } catch (IOException e1) {
                logger.error(
                        "caught IOException trying to send error response for failed resource worker",
                        e);
            }
        }

        // If the worker is still running add it to the hung-workers queue
        if (!resourceWorker.isComplete()) {
            cancelWorker(request, resourceWorker);
        }
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.rendering.IPortletExecutionManager#isPortletHeaderRenderRequested(org.apereo.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean isPortletRenderHeaderRequested(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap =
                this.getPortletHeaderRenderingMap(request);
        final IPortletRenderExecutionWorker tracker = portletRenderingMap.get(portletWindowId);

        return tracker != null;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.rendering.IPortletExecutionManager#isPortletRenderRequested(org.apereo.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public boolean isPortletRenderRequested(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap =
                this.getPortletRenderingMap(request);
        final IPortletRenderExecutionWorker tracker = portletRenderingMap.get(portletWindowId);

        return tracker != null;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.rendering.IPortletExecutionManager#getPortletHeadOutput(org.apereo.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getPortletHeadOutput(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (doesPortletNeedHeaderWorker(portletWindowId, request)) {
            final IPortletRenderExecutionWorker tracker =
                    getRenderedPortletHeaderWorker(portletWindowId, request, response);
            final long timeout = getPortletRenderTimeout(portletWindowId, request);
            try {
                final String output = tracker.getOutput(timeout);
                return output == null ? "" : output;
            } catch (Exception e) {
                logger.error("failed to render header output for " + portletWindowId, e);
                return "";
            }
        }

        logger.debug(portletWindowId + " does not produce output for header");
        return "";
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.rendering.IPortletExecutionManager#getPortletOutput(org.apereo.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getPortletOutput(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        final IPortletRenderExecutionWorker tracker =
                getRenderedPortletBodyWorker(portletWindowId, request, response);
        final long timeout = getPortletRenderTimeout(portletWindowId, request);

        try {
            final String output =
                    this.personalizer.personalize(
                            this.personManager.getPerson(request),
                            tracker.getOutput(timeout),
                            request.getSession());
            return output == null ? "" : output;
        } catch (Exception e) {
            final IPortletFailureExecutionWorker failureWorker =
                    this.portletWorkerFactory.createFailureWorker(
                            request, response, portletWindowId, e);
            // TODO publish portlet error event?
            try {
                failureWorker.submit();
                return failureWorker.getOutput(timeout);
            } catch (Exception e1) {
                logger.error("Failed to render error portlet for: " + portletWindowId, e1);
                return "Error Portlet Unavailable. Please contact your portal administrators.";
            }
        }
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.rendering.IPortletExecutionManager#getPortletTitle(org.apereo.portal.portlet.om.IPortletWindowId, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public String getPortletTitle(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        final IPortletDefinition portletDefinition = getPortletDefinition(portletWindowId, request);
        final IPortletDefinitionParameter disableDynamicTitle =
                portletDefinition.getParameter("disableDynamicTitle");

        String titleToReturn = null;

        if (disableDynamicTitle == null || !Boolean.parseBoolean(disableDynamicTitle.getValue())) {
            try {
                final PortletRenderResult portletRenderResult =
                        getPortletRenderResult(portletWindowId, request, response);

                if (portletRenderResult != null) {
                    titleToReturn = portletRenderResult.getTitle();
                }
            } catch (Exception e) {
                logger.warn(
                        "unable to get portlet title, falling back to title defined in channel definition for portletWindowId "
                                + portletWindowId);
            }
        }

        if (titleToReturn == null) {
            // we assume that response locale has been set to correct value
            String locale = response.getLocale().toString();
            titleToReturn = portletDefinition.getTitle(locale);
        }

        // Personalize the title
        titleToReturn =
                this.personalizer.personalize(
                        this.personManager.getPerson(request), titleToReturn, request.getSession());

        // return portlet title from channel definition
        return titleToReturn;
    }

    @Override
    public int getPortletNewItemCount(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            final PortletRenderResult portletRenderResult =
                    getPortletRenderResult(portletWindowId, request, response);
            if (portletRenderResult != null) {
                final int newItemCount = portletRenderResult.getNewItemCount();
                return newItemCount;
            }
        } catch (Exception e) {
            logger.warn(
                    "unable to get portlet new item count for portletWindowId " + portletWindowId);
        }

        return 0;
    }

    @Override
    public String getPortletLink(
            IPortletWindowId portletWindowId,
            String defaultPortletUrl,
            HttpServletRequest request,
            HttpServletResponse response) {
        try {
            final PortletRenderResult portletRenderResult =
                    getPortletRenderResult(portletWindowId, request, response);
            if (portletRenderResult != null) {
                final String link = portletRenderResult.getExternalLink();
                if (StringUtils.isNotBlank(link)) {
                    return link;
                } else {
                    return defaultPortletUrl;
                }
            }
        } catch (Exception e) {
            logger.warn("unable to get portlet link count for portletWindowId " + portletWindowId);
        }

        return defaultPortletUrl;
    }

    /**
     * This method handles portlets that are slow to warm up. The default config multiplies the
     * portlet's configured timeout by 20 the first 5 times it executes. The key is the portlet
     * descriptor so even if you have the same portlet (web proxy for example) published 20 times
     * only the first 5 renders of ANY WPP will get the extra time.
     *
     * @param portletDefinition
     * @param request
     * @param timeout
     * @return
     */
    protected final long getModifiedTimeout(
            IPortletDefinition portletDefinition, HttpServletRequest request, long timeout) {
        final IPortletDescriptorKey portletDescriptorKey =
                portletDefinition.getPortletDescriptorKey();
        final AtomicInteger counter = this.executionCount.get(portletDescriptorKey);
        final int executionCount = counter.get();

        if (executionCount > extendedTimeoutExecutions) {
            return timeout;
        }

        if (logger.isDebugEnabled()) {
            logger.debug(
                    String.format(
                            "Modifying timeout for %40s from %7s to %8s on execution %2s\n",
                            portletDescriptorKey.toString(),
                            timeout,
                            timeout * extendedTimeoutMultiplier,
                            executionCount));
        }
        return timeout * extendedTimeoutMultiplier;
    }

    protected long getPortletActionTimeout(
            IPortletWindowId portletWindowId, HttpServletRequest request) {
        if (this.ignoreTimeouts) {
            return DEBUG_TIMEOUT;
        }

        final IPortletDefinition portletDefinition = getPortletDefinition(portletWindowId, request);
        final Integer actionTimeout = portletDefinition.getActionTimeout();
        if (actionTimeout != null) {
            return getModifiedTimeout(portletDefinition, request, actionTimeout);
        }

        return getModifiedTimeout(portletDefinition, request, portletDefinition.getTimeout());
    }

    protected long getPortletEventTimeout(
            IPortletWindowId portletWindowId, HttpServletRequest request) {
        if (this.ignoreTimeouts) {
            return DEBUG_TIMEOUT;
        }

        final IPortletDefinition portletDefinition = getPortletDefinition(portletWindowId, request);
        final Integer eventTimeout = portletDefinition.getEventTimeout();
        if (eventTimeout != null) {
            return getModifiedTimeout(portletDefinition, request, eventTimeout);
        }

        return getModifiedTimeout(portletDefinition, request, portletDefinition.getTimeout());
    }

    protected long getPortletRenderTimeout(
            IPortletWindowId portletWindowId, HttpServletRequest request) {
        if (this.ignoreTimeouts) {
            return DEBUG_TIMEOUT;
        }

        final IPortletDefinition portletDefinition = getPortletDefinition(portletWindowId, request);
        final Integer renderTimeout = portletDefinition.getRenderTimeout();
        if (renderTimeout != null) {
            return getModifiedTimeout(portletDefinition, request, renderTimeout);
        }

        return getModifiedTimeout(portletDefinition, request, portletDefinition.getTimeout());
    }

    protected long getPortletResourceTimeout(
            IPortletWindowId portletWindowId, HttpServletRequest request) {
        if (this.ignoreTimeouts) {
            return DEBUG_TIMEOUT;
        }

        final IPortletDefinition portletDefinition = getPortletDefinition(portletWindowId, request);
        final Integer resourceTimeout = portletDefinition.getResourceTimeout();
        if (resourceTimeout != null) {
            return getModifiedTimeout(portletDefinition, request, resourceTimeout);
        }

        return getModifiedTimeout(portletDefinition, request, portletDefinition.getTimeout());
    }

    protected IPortletDefinition getPortletDefinition(
            IPortletWindowId portletWindowId, HttpServletRequest request) {
        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(request, portletWindowId);
        final IPortletEntity parentPortletEntity = portletWindow.getPortletEntity();
        return parentPortletEntity.getPortletDefinition();
    }

    protected IPortletRenderExecutionWorker getRenderedPortletHeaderWorker(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletHeaderRenderingMap =
                this.getPortletHeaderRenderingMap(request);
        IPortletRenderExecutionWorker portletHeaderRenderWorker =
                portletHeaderRenderingMap.get(portletWindowId);
        if (portletHeaderRenderWorker == null) {
            portletHeaderRenderWorker =
                    this.startPortletHeaderRenderInternal(portletWindowId, request, response);
        }
        return portletHeaderRenderWorker;
    }

    protected IPortletRenderExecutionWorker getRenderedPortletBodyWorker(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap =
                this.getPortletRenderingMap(request);
        IPortletRenderExecutionWorker tracker = portletRenderingMap.get(portletWindowId);
        if (tracker == null) {
            tracker = this.startPortletRenderInternal(portletWindowId, request, response);
        }
        return tracker;
    }

    /**
     * Returns the PortletRenderResult waiting up to the portlet's timeout
     *
     * @return The PortletRenderResult from the portlet's execution
     * @throws TimeoutException If the portlet's timeout was hit before a result was returned
     * @throws Exception The exception thrown by the portlet during execution
     */
    protected PortletRenderResult getPortletRenderResult(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response)
            throws Exception {
        final IPortletRenderExecutionWorker tracker =
                getRenderedPortletBodyWorker(portletWindowId, request, response);
        final long timeout = getPortletRenderTimeout(portletWindowId, request);
        return tracker.get(timeout);
    }

    /**
     * create and submit the portlet header rendering job to the thread pool
     *
     * @param portletWindowId
     * @param request
     * @param response
     * @return
     */
    protected IPortletRenderExecutionWorker startPortletHeaderRenderInternal(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        IPortletRenderExecutionWorker portletHeaderRenderWorker =
                this.portletWorkerFactory.createRenderHeaderWorker(
                        request, response, portletWindowId);
        portletHeaderRenderWorker.submit();

        final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletHeaderRenderingMap =
                this.getPortletHeaderRenderingMap(request);
        portletHeaderRenderingMap.put(portletWindowId, portletHeaderRenderWorker);

        return portletHeaderRenderWorker;
    }

    /** create and submit the portlet content rendering job to the thread pool */
    protected IPortletRenderExecutionWorker startPortletRenderInternal(
            IPortletWindowId portletWindowId,
            HttpServletRequest request,
            HttpServletResponse response) {
        // first check to see if there is a Throwable in the session for this IPortletWindowId
        final Map<IPortletWindowId, Exception> portletFailureMap = getPortletErrorMap(request);
        final Exception cause = portletFailureMap.remove(portletWindowId);

        final IPortletRenderExecutionWorker portletRenderExecutionWorker;
        if (null != cause) {
            // previous action failed, dispatch to errorPortlet immediately
            portletRenderExecutionWorker =
                    this.portletWorkerFactory.createFailureWorker(
                            request, response, portletWindowId, cause);
        } else {
            IPortletWindow portletWindow =
                    portletWindowRegistry.getPortletWindow(request, portletWindowId);
            IPortletDefinition portletDef = portletWindow.getPortletEntity().getPortletDefinition();
            if (portletDef.getLifecycleState().equals(PortletLifecycleState.MAINTENANCE)) {
                // Prevent the portlet from rendering;  replace with a helpful "Out of Service"
                // message
                final IPortletDefinitionParameter messageParam =
                        portletDef.getParameter(
                                PortletLifecycleState.CUSTOM_MAINTENANCE_MESSAGE_PARAMETER_NAME);
                final Exception mme =
                        messageParam != null && StringUtils.isNotBlank(messageParam.getValue())
                                ? new MaintenanceModeException(messageParam.getValue())
                                : new MaintenanceModeException();
                portletRenderExecutionWorker =
                        this.portletWorkerFactory.createFailureWorker(
                                request, response, portletWindowId, mme);
            } else {
                // Happy path
                portletRenderExecutionWorker =
                        this.portletWorkerFactory.createRenderWorker(
                                request, response, portletWindowId);
            }
        }

        portletRenderExecutionWorker.submit();

        final Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap =
                this.getPortletRenderingMap(request);
        portletRenderingMap.put(portletWindowId, portletRenderExecutionWorker);

        return portletRenderExecutionWorker;
    }

    /**
     * Returns a request attribute scoped Map of portlets that are rendering for the current
     * request.
     */
    @SuppressWarnings("unchecked")
    protected Map<IPortletWindowId, IPortletRenderExecutionWorker> getPortletHeaderRenderingMap(
            HttpServletRequest request) {
        synchronized (PortalWebUtils.getRequestAttributeMutex(request)) {
            Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap =
                    (Map<IPortletWindowId, IPortletRenderExecutionWorker>)
                            request.getAttribute(PORTLET_HEADER_RENDERING_MAP);
            if (portletRenderingMap == null) {
                portletRenderingMap =
                        new ConcurrentHashMap<IPortletWindowId, IPortletRenderExecutionWorker>();
                request.setAttribute(PORTLET_HEADER_RENDERING_MAP, portletRenderingMap);
            }
            return portletRenderingMap;
        }
    }
    /**
     * Returns a request attribute scoped Map of portlets that are rendering for the current
     * request.
     */
    @SuppressWarnings("unchecked")
    protected Map<IPortletWindowId, IPortletRenderExecutionWorker> getPortletRenderingMap(
            HttpServletRequest request) {
        synchronized (PortalWebUtils.getRequestAttributeMutex(request)) {
            Map<IPortletWindowId, IPortletRenderExecutionWorker> portletRenderingMap =
                    (Map<IPortletWindowId, IPortletRenderExecutionWorker>)
                            request.getAttribute(PORTLET_RENDERING_MAP);
            if (portletRenderingMap == null) {
                portletRenderingMap =
                        new ConcurrentHashMap<IPortletWindowId, IPortletRenderExecutionWorker>();
                request.setAttribute(PORTLET_RENDERING_MAP, portletRenderingMap);
            }
            return portletRenderingMap;
        }
    }

    /**
     * Null safe means for retrieving the {@link Map} from the specified session keyed by {@link
     * #SESSION_ATTRIBUTE__PORTLET_FAILURE_CAUSE_MAP}.
     *
     * @param request HttpServletRequest
     * @return a never null {@link Map} in the session for storing portlet failure causes.
     */
    @SuppressWarnings("unchecked")
    protected Map<IPortletWindowId, Exception> getPortletErrorMap(HttpServletRequest request) {
        final HttpSession session = request.getSession();
        synchronized (WebUtils.getSessionMutex(session)) {
            Map<IPortletWindowId, Exception> portletFailureMap =
                    (Map<IPortletWindowId, Exception>)
                            session.getAttribute(SESSION_ATTRIBUTE__PORTLET_FAILURE_CAUSE_MAP);
            if (portletFailureMap == null) {
                portletFailureMap = new ConcurrentHashMap<IPortletWindowId, Exception>();
                session.setAttribute(
                        SESSION_ATTRIBUTE__PORTLET_FAILURE_CAUSE_MAP, portletFailureMap);
            }
            return portletFailureMap;
        }
    }
}
