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

package org.jasig.portal.portlet.rendering;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.portlet.Event;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.namespace.QName;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.apache.commons.lang.StringUtils;
import org.apache.pluto.container.PortletContainer;
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.PortletWindow;
import org.apache.pluto.container.driver.PortletContextService;
import org.apache.pluto.container.om.portlet.EventDefinition;
import org.apache.pluto.container.om.portlet.EventDefinitionReference;
import org.apache.pluto.container.om.portlet.PortletApplicationDefinition;
import org.apache.pluto.container.om.portlet.PortletDefinition;
import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.portlet.container.EventImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
import org.jasig.portal.portlet.registry.IPortletWindowRegistry;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.jasig.portal.utils.web.PortalWebUtils;
import org.jasig.portal.xml.XmlUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.stereotype.Service;

/**
 * uPortal's approach to event coordination is to simply queue the events and rely on the {@link PortletExecutionManager}
 * to handle event execution. What this class does is for each {@link #processEvents(PortletContainer, PortletWindow, HttpServletRequest, HttpServletResponse, List)}
 * request from the portlet container is to add them to a Queue scoped to the portal's request.
 * 
 * It also provides {@link #resolveQueueEvents(PortletEventQueue, List, HttpServletRequest)} which is used to determine
 * which events to send to which portlet windows. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("eventCoordinationService")
public class PortletEventCoordinatationService implements IPortletEventCoordinationService {
    private static final String PORTLET_EVENT_QUEUE = PortletEventCoordinatationService.class.getName() + ".PORTLET_EVENT_QUEUE";
    
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
            @Qualifier("org.jasig.portal.portlet.rendering.SupportedEventCache") Ehcache supportedEventCache) {
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

    /**
     * Returns a request attribute scoped Map of portlets that have set events to be processed for the current request
     */
    @Override
    @SuppressWarnings("unchecked")
    public Queue<Event> getQueuedEvents(HttpServletRequest request) {
        request = this.portalRequestUtils.getOriginalPortalRequest(request);
        
        synchronized (PortalWebUtils.getRequestAttributeMutex(request)) {
            Queue<Event> portletEventQueue = (Queue<Event>)request.getAttribute(PORTLET_EVENT_QUEUE);
            if (portletEventQueue == null) {
                portletEventQueue = new ConcurrentLinkedQueue<Event>();
                request.setAttribute(PORTLET_EVENT_QUEUE, portletEventQueue);
            }
            return portletEventQueue;
        }
    }
    
    @Override
    public void processEvents(PortletContainer container, PortletWindow plutoPortletWindow, HttpServletRequest request, HttpServletResponse response, List<Event> events) {
        final Queue<Event> queuedEvents = this.getQueuedEvents(request);
        queuedEvents.addAll(events);
    }
    
    @Override
    public void resolveQueueEvents(PortletEventQueue resolvedEvents, Queue<Event> events, HttpServletRequest request) {
        //Skip all processing if there are no new events.
        if (events.isEmpty()) {
            return;
        }

        //Get all the portlets the user is subscribed to
        final IUserInstance userInstance = this.userInstanceManager.getUserInstance(request);
        final IUserPreferencesManager preferencesManager = userInstance.getPreferencesManager();
        final IUserLayoutManager userLayoutManager = preferencesManager.getUserLayoutManager();
        final Set<String> allSubscribedChannels = userLayoutManager.getAllSubscribedChannels();
        
        //Check each subscription to see what events it is registered to see
        for (final String channelSubscribeId : allSubscribedChannels) {
            final IPortletEntity portletEntity = this.portletEntityRegistry.getOrCreatePortletEntity(request, userInstance, channelSubscribeId);
            final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
            final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
            
            final PortletDefinition portletDescriptor;
            try {
                portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId);
            }
            catch (DataRetrievalFailureException e) {
                this.logger.warn("Failed to retrieve portlet descriptor for: " + portletDefinition.getFName() + " Event handling for this portlet will be skipped." , e);
                continue;
            }
            
            final List<? extends EventDefinitionReference> supportedProcessingEvents = portletDescriptor.getSupportedProcessingEvents();
            //Skip portlets that don't handle any events
            if (supportedProcessingEvents == null || supportedProcessingEvents.size() == 0) {
                continue;
            }
            
            //Check each published event against the events the portlet supports
            while (!events.isEmpty()) {
                final Event event = events.poll();
                if (event == null) {
                    break;
                }

                if (this.supportsEvent(event, portletDefinitionId)) {
                    final IPortletEntityId portletEntityId = portletEntity.getPortletEntityId();
                    final Set<IPortletWindow> portletWindows = this.portletWindowRegistry.getAllPortletWindowsForEntity(request, portletEntityId);
                    
                    for (final IPortletWindow portletWindow : portletWindows) {
                        final IPortletWindowId portletWindowId = portletWindow.getPortletWindowId();
                        final Event unmarshalledEvent = this.unmarshall(portletWindow, event);
                        resolvedEvents.offerEvent(portletWindowId, unmarshalledEvent);
                    }
                }
            }
        }
    }

    protected Event unmarshall(IPortletWindow portletWindow, Event event) {
        //TODO make two types of Event impls, one for marshalled data and one for unmarshalled data
        String value = (String)event.getValue();
        
        final XMLInputFactory xmlInputFactory = this.xmlUtilities.getXmlInputFactory();
        final XMLStreamReader xml;
        try {
            xml = xmlInputFactory.createXMLStreamReader(new StringReader(value));
        }
        catch (XMLStreamException e) {
            throw new IllegalStateException("Failed to create XMLStreamReader for portlet event: " + event, e);
        }
        
        // now test if object is jaxb
        final EventDefinition eventDefinitionDD = getEventDefintion(portletWindow, event.getQName()); 
        
        final PortletDefinition portletDefinition = portletWindow.getPlutoPortletWindow().getPortletDefinition();
        final PortletApplicationDefinition application = portletDefinition.getApplication();
        final String portletApplicationName = application.getName();
        
        final ClassLoader loader;
        try {
            loader = portletContextService.getClassLoader(portletApplicationName);
        }
        catch (PortletContainerException e) {
            throw new IllegalStateException("Failed to get ClassLoader for portlet application: " + portletApplicationName, e);
        }
        
        final String eventType = eventDefinitionDD.getValueType();
        final Class<? extends Serializable> clazz;
        try {
            clazz = loader.loadClass(eventType).asSubclass(Serializable.class);
        }
        catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("Declared event type '" + eventType + "' cannot be found in portlet application: " + portletApplicationName, e);
        }
    
        //TODO cache JAXBContext in registered portlet application
        final JAXBElement<? extends Serializable> result;
        try {
            final JAXBContext jc = JAXBContext.newInstance(clazz);
            final Unmarshaller unmarshaller = jc.createUnmarshaller();
            result = unmarshaller.unmarshal(xml, clazz);
        }
        catch (JAXBException e) {
            throw new IllegalArgumentException("Cannot create JAXBContext for event type '" + eventType + "' from portlet application: " + portletApplicationName, e);
        }
    
        return new EventImpl(event.getQName(), result.getValue());
    }

    //TODO cache this resolution
    protected EventDefinition getEventDefintion(IPortletWindow portletWindow, QName name) {
        PortletApplicationDefinition appDD = portletWindow.getPlutoPortletWindow().getPortletDefinition().getApplication();
        for (EventDefinition def : appDD.getEventDefinitions()) {
            if (def.getQName() != null) {
                if (def.getQName().equals(name))
                    return def;
            }
            else {
                QName tmp = new QName(appDD.getDefaultNamespace(), def.getName());
                if (tmp.equals(name))
                    return def;
            }
        }
        throw new IllegalStateException();
    }

    protected Set<QName> getAllAliases(QName eventName, PortletApplicationDefinition portletApplicationDefinition) {
        final List<? extends EventDefinition> eventDefinitions = portletApplicationDefinition.getEventDefinitions();
        if (eventDefinitions == null) {
            return Collections.emptySet();
        }

        for (final EventDefinition eventDefinition : eventDefinitions) {
            final String defaultNamespace = portletApplicationDefinition.getDefaultNamespace();
            final QName defQName = eventDefinition.getQualifiedName(defaultNamespace);
            if (defQName != null && defQName.equals(eventName)) {
                return new LinkedHashSet<QName>(eventDefinition.getAliases());
            }
        }

        return Collections.emptySet();
    }
    
    protected boolean supportsEvent(Event event, IPortletDefinitionId portletDefinitionId) {
        final QName eventName = event.getQName();
        
        //Check in the cache if this event has already been resolved for this portlet definition
        final Map<QName, Boolean> supportedEvents = this.getSupportedEventsCache(portletDefinitionId);
        
        final Boolean supported = supportedEvents.get(eventName);
        if (supported != null) {
            return supported;
        }
        
        try {
            final PortletApplicationDefinition portletApplicationDescriptor = this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(portletDefinitionId);
            final Set<QName> aliases = this.getAllAliases(eventName, portletApplicationDescriptor);
            
            final String defaultNamespace = portletApplicationDescriptor.getDefaultNamespace();
            
            //No support found so far, do more complex namespace matching
            final PortletDefinition portletDescriptor = this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId);
            final List<? extends EventDefinitionReference> supportedProcessingEvents = portletDescriptor.getSupportedProcessingEvents();
            for (final EventDefinitionReference eventDefinitionReference : supportedProcessingEvents) {
                final QName qualifiedName = eventDefinitionReference.getQualifiedName(defaultNamespace);
                if (qualifiedName == null) {
                    continue;
                }
                
                //See if the supported qname and event qname match explicitly
                if (qualifiedName.equals(eventName)) {
                    supportedEvents.put(eventName, true);                
                    return true;
                }
                
                //Look for alias names
                if (aliases.contains(qualifiedName)) {
                    supportedEvents.put(eventName, true);
                    return true;
                }
                
                //Look for namespaced events
                if (StringUtils.isEmpty(qualifiedName.getNamespaceURI())) {
                    final QName namespacedName = new QName(defaultNamespace, qualifiedName.getLocalPart());
                    if (eventName.equals(namespacedName)) {
                        supportedEvents.put(eventName, true);
                        return true;
                    }
                }
            }
            
    
            supportedEvents.put(eventName, false);
            return false;
        }
        finally {
            this.supportedEventCache.put(new Element(portletDefinitionId, supportedEvents));
        }
    }

    /**
     * Get a Map of event names that have already been resolved against this portlet definition
     */
    @SuppressWarnings("unchecked")
    protected Map<QName, Boolean> getSupportedEventsCache(IPortletDefinitionId portletDefinitionId) {
        Map<QName, Boolean> supportedEvents = null;
        final Element supportedEventsElement = this.supportedEventCache.get(portletDefinitionId);
        if (supportedEventsElement != null) {
            supportedEvents = (Map<QName, Boolean>)supportedEventsElement.getObjectValue();
        }
        if (supportedEvents == null) {
            supportedEvents = new ConcurrentHashMap<QName, Boolean>();
        }
        return supportedEvents;
    }
}
