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
import com.google.common.collect.Lists;
import java.util.*;
import javax.portlet.Event;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import net.sf.ehcache.Ehcache;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.driver.PortletContextService;
import org.apache.pluto.container.om.portlet.EventDefinitionReference;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.IUserPreferencesManager;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.portlet.om.*;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlet.registry.IPortletEntityRegistry;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.services.AuthorizationServiceFacade;
import org.apereo.portal.url.IPortalRequestUtils;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.apereo.portal.utils.web.PortalWebUtils;
import org.apereo.portal.xml.XmlUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/**
 * uPortal's approach to event coordination is to simply queue the events and rely on the {@link
 * IPortletExecutionManager} to handle event execution. What this class does is for each {@link
 * #processEvents(PortletContainer, PortletWindow, HttpServletRequest, HttpServletResponse, List)}
 * request from the portlet container is to add them to a Queue scoped to the portal's request.
 *
 * <p>It also provides {@link #getPortletEventQueue(PortletEventQueue, List, HttpServletRequest)}
 * which is used to determine which events to send to which portlet windows.
 */
@Service("eventCoordinationService")
public class PortletEventCoordinatationService implements IPortletEventCoordinationService {

    private static final String PORTLET_EVENT_QUEUE =
            PortletEventCoordinatationService.class.getName() + ".PORTLET_EVENT_QUEUE";

    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private IPortletWindowRegistry portletWindowRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IUserInstanceManager userInstanceManager;
    private Ehcache supportedEventCache;
    private IPortalRequestUtils portalRequestUtils;
    private XmlUtilities xmlUtilities;
    private PortletContextService portletContextService;

    @Autowired
    public void setPortletContextService(PortletContextService portletContextService) {
        this.portletContextService = portletContextService;
    }

    @Autowired
    public void setXmlUtilities(XmlUtilities xmlUtilities) {
        this.xmlUtilities = xmlUtilities;
    }

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Autowired
    public void setSupportedEventCache(
            @Qualifier("org.apereo.portal.portlet.rendering.SupportedEventCache")
                    Ehcache supportedEventCache) {
        this.supportedEventCache = supportedEventCache;
    }

    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    @Autowired
    public void setUserInstanceManager(IUserInstanceManager userInstanceManager) {
        this.userInstanceManager = userInstanceManager;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }

    protected final PortletEventCoordinationHelper portletEventCoordinationHelper =
            new PortletEventCoordinationHelper(
                    this.xmlUtilities,
                    this.portletContextService,
                    this.portletWindowRegistry,
                    this.supportedEventCache,
                    this.portletDefinitionRegistry);

    /**
     * Returns a request scoped PortletEventQueue used to track events to process and events to
     * dispatch
     */
    @Override
    public PortletEventQueue getPortletEventQueue(HttpServletRequest request) {
        request = this.portalRequestUtils.getOriginalPortalRequest(request);

        synchronized (PortalWebUtils.getRequestAttributeMutex(request)) {
            PortletEventQueue portletEventQueue =
                    (PortletEventQueue) request.getAttribute(PORTLET_EVENT_QUEUE);
            if (portletEventQueue == null) {
                portletEventQueue = new PortletEventQueue();
                request.setAttribute(PORTLET_EVENT_QUEUE, portletEventQueue);
            }
            return portletEventQueue;
        }
    }

    @Override
    public void processEvents(
            PortletContainer container,
            PortletWindow plutoPortletWindow,
            HttpServletRequest request,
            HttpServletResponse response,
            List<Event> events) {
        final PortletEventQueue requestPortletEventQueue = this.getPortletEventQueue(request);
        this.logger.debug("Queued {} from {}", events, plutoPortletWindow);

        final IPortletWindow portletWindow =
                this.portletWindowRegistry.convertPortletWindow(request, plutoPortletWindow);
        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();

        // Add list transformer to convert Event to QueuedEvent
        final List<QueuedEvent> queuedEvents =
                Lists.transform(
                        events,
                        new Function<Event, QueuedEvent>() {
                            /* (non-Javadoc)
                             * @see com.google.common.base.Function#apply(java.lang.Object)
                             */
                            @Override
                            public QueuedEvent apply(Event event) {
                                return new QueuedEvent(portletWindowId, event);
                            }
                        });

        requestPortletEventQueue.addEvents(queuedEvents);
    }

    @Override
    public void resolvePortletEvents(
            HttpServletRequest request, PortletEventQueue portletEventQueue) {
        final Queue<QueuedEvent> events = portletEventQueue.getUnresolvedEvents();

        // Skip all processing if there are no new events.
        if (events.isEmpty()) {
            return;
        }

        // Get all the portlets the user is subscribed to
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();

        // Make a local copy so we can remove data from it
        final Set<String> allLayoutNodeIds =
                new LinkedHashSet<String>(userLayoutManager.getAllSubscribedChannels());

        final Map<String, IPortletEntity> portletEntityCache =
                new LinkedHashMap<String, IPortletEntity>();

        while (!events.isEmpty()) {
            final QueuedEvent queuedEvent = events.poll();
            if (queuedEvent == null) {
                // no more queued events, done resolving
                return;
            }

            final IPortletWindowId sourceWindowId = queuedEvent.getPortletWindowId();
            final Event event = queuedEvent.getEvent();

            final boolean globalEvent =
                    this.portletEventCoordinationHelper.isGlobalEvent(
                            request, sourceWindowId, event);

            final Set<IPortletDefinition> portletDefinitions =
                    new LinkedHashSet<IPortletDefinition>();
            if (globalEvent) {
                portletDefinitions.addAll(
                        this.portletDefinitionRegistry.getAllPortletDefinitions());
            }

            // Check each subscription to see what events it is registered to see
            for (final Iterator<String> layoutNodeIdItr = allLayoutNodeIds.iterator();
                    layoutNodeIdItr.hasNext(); ) {
                final String layoutNodeId = layoutNodeIdItr.next();

                IPortletEntity portletEntity = portletEntityCache.get(layoutNodeId);
                if (portletEntity == null) {
                    portletEntity =
                            this.portletEntityRegistry.getOrCreatePortletEntity(
                                    request, userInstance, layoutNodeId);

                    // if portlet entity registry returned null, then portlet has been deleted -
                    // remove it (see UP-3378)
                    if (portletEntity == null) {
                        layoutNodeIdItr.remove();
                        continue;
                    }

                    final IPortletDefinitionId portletDefinitionId =
                            portletEntity.getPortletDefinitionId();
                    final PortletDefinition portletDescriptor =
                            this.portletDefinitionRegistry.getParentPortletDescriptor(
                                    portletDefinitionId);
                    if (portletDescriptor == null) {
                        // Missconfigured portlet, remove it from the list so we don't check again
                        // and ignore it
                        layoutNodeIdItr.remove();
                        continue;
                    }

                    final List<? extends EventDefinitionReference> supportedProcessingEvents =
                            portletDescriptor.getSupportedProcessingEvents();
                    // Skip portlets that don't handle any events and remove them from the set so
                    // they are not checked again
                    if (supportedProcessingEvents == null
                            || supportedProcessingEvents.size() == 0) {
                        layoutNodeIdItr.remove();
                        continue;
                    }

                    portletEntityCache.put(layoutNodeId, portletEntity);
                }

                final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
                final IPortletDefinitionId portletDefinitionId =
                        portletDefinition.getPortletDefinitionId();
                if (portletEventCoordinationHelper.supportsEvent(event, portletDefinitionId)) {
                    this.logger.debug("{} supports event {}", portletDefinition, event);

                    // If this is the default portlet entity remove the definition from the all defs
                    // set to avoid duplicate processing
                    final IPortletEntity defaultPortletEntity =
                            this.portletEntityRegistry.getOrCreateDefaultPortletEntity(
                                    request, portletDefinitionId);
                    if (defaultPortletEntity.equals(portletEntity)) {
                        portletDefinitions.remove(portletDefinition);
                    }

                    // Is this portlet permitted to receive events?  (Or is it
                    // disablePortletEvents=true?)
                    IPortletDefinitionParameter disablePortletEvents =
                            portletDefinition.getParameter(
                                    PortletExecutionManager.DISABLE_PORTLET_EVENTS_PARAMETER);
                    if (disablePortletEvents != null
                            && Boolean.parseBoolean(disablePortletEvents.getValue())) {
                        logger.info(
                                "Ignoring portlet events for portlet '{}' because they have been disabled.",
                                portletDefinition.getFName());
                        continue;
                    }

                    final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
                    final Set<IPortletWindow> portletWindows =
                            this.portletWindowRegistry.getAllPortletWindowsForEntity(
                                    request, portletEntityId);

                    for (final IPortletWindow portletWindow : portletWindows) {
                        this.logger.debug("{} resolved target {}", event, portletWindow);
                        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
                        final Event unmarshalledEvent =
                                portletEventCoordinationHelper.unmarshall(portletWindow, event);
                        portletEventQueue.offerEvent(
                                portletWindowId,
                                new QueuedEvent(sourceWindowId, unmarshalledEvent));
                    }
                } else {
                    portletDefinitions.remove(portletDefinition);
                }
            }

            if (!portletDefinitions.isEmpty()) {
                final IPerson user = userInstance.getPerson();
                final EntityIdentifier ei = user.getEntityIdentifier();
                final IAuthorizationPrincipal ap =
                        AuthorizationServiceFacade.instance()
                                .newPrincipal(ei.getKey(), ei.getType());

                // If the event is global there might still be portlet definitions that need
                // targeting
                for (final IPortletDefinition portletDefinition : portletDefinitions) {

                    // Is this portlet permitted to receive events?  (Or is it
                    // disablePortletEvents=true?)
                    IPortletDefinitionParameter disablePortletEvents =
                            portletDefinition.getParameter(
                                    PortletExecutionManager.DISABLE_PORTLET_EVENTS_PARAMETER);
                    if (disablePortletEvents != null
                            && Boolean.parseBoolean(disablePortletEvents.getValue())) {
                        logger.info(
                                "Ignoring portlet events for portlet '{}' because they have been disabled.",
                                portletDefinition.getFName());
                        continue;
                    }

                    final IPortletDefinitionId portletDefinitionId =
                            portletDefinition.getPortletDefinitionId();
                    // Check if the user can render the portlet definition before doing event tests
                    if (ap.canRender(portletDefinitionId.getStringId())) {
                        if (portletEventCoordinationHelper.supportsEvent(
                                event, portletDefinitionId)) {
                            this.logger.debug("{} supports event {}", portletDefinition, event);

                            final IPortletEntity portletEntity =
                                    this.portletEntityRegistry.getOrCreateDefaultPortletEntity(
                                            request, portletDefinitionId);
                            final IPortletEntityId portletEntityId =
                                    portletEntity.getPortletEntityId();
                            final Set<IPortletWindow> portletWindows =
                                    this.portletWindowRegistry.getAllPortletWindowsForEntity(
                                            request, portletEntityId);

                            for (final IPortletWindow portletWindow : portletWindows) {
                                this.logger.debug("{} resolved target {}", event, portletWindow);
                                final IPortletWindowId portletWindowId =
                                        portletWindow.getPortletWindowId();
                                final Event unmarshalledEvent =
                                        portletEventCoordinationHelper.unmarshall(
                                                portletWindow, event);
                                portletEventQueue.offerEvent(
                                        portletWindowId,
                                        new QueuedEvent(sourceWindowId, unmarshalledEvent));
                            }
                        }
                    }
                }
            }
        }
    }
}
