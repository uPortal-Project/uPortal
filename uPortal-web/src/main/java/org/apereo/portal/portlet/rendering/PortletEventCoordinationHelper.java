package org.apereo.portal.portlet.rendering;

import java.io.Serializable;
import java.io.StringReader;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import javax.portlet.Event;
import javax.servlet.http.HttpServletRequest;
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
import org.apache.pluto.container.PortletContainerException;
import org.apache.pluto.container.driver.PortletContextService;
import org.apache.pluto.container.om.portlet.*;
import org.apereo.portal.portlet.container.EventImpl;
import org.apereo.portal.portlet.om.*;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.utils.Tuple;
import org.apereo.portal.xml.XmlUtilities;
import org.springframework.stereotype.Component;

@Component
public class PortletEventCoordinationHelper {
    public static final String GLOBAL_EVENT__CONTAINER_OPTION = "org.apereo.portal.globalEvent";
    private XmlUtilities xmlUtilities;
    private PortletContextService portletContextService;
    private IPortletWindowRegistry portletWindowRegistry;
    private Ehcache supportedEventCache;

    public PortletEventCoordinationHelper(
            XmlUtilities xmlUtilities,
            PortletContextService portletContextService,
            IPortletWindowRegistry portletWindowRegistry,
            Ehcache supportedEventCache,
            IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.xmlUtilities = xmlUtilities;
        this.portletContextService = portletContextService;
        this.portletWindowRegistry = portletWindowRegistry;
        this.supportedEventCache = supportedEventCache;
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }

    private IPortletDefinitionRegistry portletDefinitionRegistry;

    protected Event unmarshall(IPortletWindow portletWindow, Event event) {
        // TODO make two types of Event impls, one for marshalled data and one for unmarshalled data
        String value = (String) event.getValue();

        final XMLInputFactory xmlInputFactory = this.xmlUtilities.getXmlInputFactory();
        final XMLStreamReader xml;
        try {
            xml = xmlInputFactory.createXMLStreamReader(new StringReader(value));
        } catch (XMLStreamException e) {
            throw new IllegalStateException(
                    "Failed to create XMLStreamReader for portlet event: " + event, e);
        }

        // now test if object is jaxb
        final EventDefinition eventDefinitionDD =
                getEventDefinition(portletWindow, event.getQName());

        final PortletDefinition portletDefinition =
                portletWindow.getPlutoPortletWindow().getPortletDefinition();
        final PortletApplicationDefinition application = portletDefinition.getApplication();
        final String portletApplicationName = application.getName();

        final ClassLoader loader;
        try {
            loader = portletContextService.getClassLoader(portletApplicationName);
        } catch (PortletContainerException e) {
            throw new IllegalStateException(
                    "Failed to get ClassLoader for portlet application: " + portletApplicationName,
                    e);
        }

        final String eventType = eventDefinitionDD.getValueType();
        final Class<? extends Serializable> clazz;
        try {
            clazz = loader.loadClass(eventType).asSubclass(Serializable.class);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException(
                    "Declared event type '"
                            + eventType
                            + "' cannot be found in portlet application: "
                            + portletApplicationName,
                    e);
        }

        // TODO cache JAXBContext in registered portlet application
        final JAXBElement<? extends Serializable> result;
        try {
            final JAXBContext jc = JAXBContext.newInstance(clazz);
            final Unmarshaller unmarshaller = jc.createUnmarshaller();
            result = unmarshaller.unmarshal(xml, clazz);
        } catch (JAXBException e) {
            throw new IllegalArgumentException(
                    "Cannot create JAXBContext for event type '"
                            + eventType
                            + "' from portlet application: "
                            + portletApplicationName,
                    e);
        }

        return new EventImpl(event.getQName(), result.getValue());
    }

    protected boolean isGlobalEvent(
            HttpServletRequest request, IPortletWindowId sourceWindowId, Event event) {
        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(request, sourceWindowId);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
        final PortletApplicationDefinition parentPortletApplicationDescriptor =
                this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(
                        portletDefinitionId);

        final ContainerRuntimeOption globalEvents =
                parentPortletApplicationDescriptor.getContainerRuntimeOption(
                        GLOBAL_EVENT__CONTAINER_OPTION);
        if (globalEvents != null) {
            final QName qName = event.getQName();
            final String qNameStr = qName.toString();
            for (final String globalEvent : globalEvents.getValues()) {
                if (qNameStr.equals(globalEvent)) {
                    return true;
                }
            }
        }

        return false;
    }

    // TODO cache this resolution
    protected EventDefinition getEventDefinition(IPortletWindow portletWindow, QName name) {
        PortletApplicationDefinition appDD =
                portletWindow.getPlutoPortletWindow().getPortletDefinition().getApplication();
        for (EventDefinition def : appDD.getEventDefinitions()) {
            if (def.getQName() != null) {
                if (def.getQName().equals(name)) return def;
            } else {
                QName tmp = new QName(appDD.getDefaultNamespace(), def.getName());
                if (tmp.equals(name)) return def;
            }
        }
        throw new IllegalStateException();
    }

    protected Set<QName> getAllAliases(
            QName eventName, PortletApplicationDefinition portletApplicationDefinition) {
        final List<? extends EventDefinition> eventDefinitions =
                portletApplicationDefinition.getEventDefinitions();
        if (eventDefinitions == null || eventDefinitions.isEmpty()) {
            return Collections.emptySet();
        }

        final String defaultNamespace = portletApplicationDefinition.getDefaultNamespace();

        for (final EventDefinition eventDefinition : eventDefinitions) {
            final QName defQName = eventDefinition.getQualifiedName(defaultNamespace);
            if (defQName != null && defQName.equals(eventName)) {
                final List<QName> aliases = eventDefinition.getAliases();
                if (aliases == null || aliases.isEmpty()) {
                    return Collections.emptySet();
                }

                return new LinkedHashSet<QName>(aliases);
            }
        }

        return Collections.emptySet();
    }

    protected boolean supportsEvent(Event event, IPortletDefinitionId portletDefinitionId) {
        final QName eventName = event.getQName();

        // The cache key to use
        final Tuple<IPortletDefinitionId, QName> key =
                new Tuple<IPortletDefinitionId, QName>(portletDefinitionId, eventName);

        // Check in the cache if the portlet definition supports this event
        final Element element = this.supportedEventCache.get(key);
        if (element != null) {
            final Boolean supported = (Boolean) element.getObjectValue();
            if (supported != null) {
                return supported;
            }
        }

        final PortletApplicationDefinition portletApplicationDescriptor =
                this.portletDefinitionRegistry.getParentPortletApplicationDescriptor(
                        portletDefinitionId);
        if (portletApplicationDescriptor == null) {
            return false;
        }

        final Set<QName> aliases = this.getAllAliases(eventName, portletApplicationDescriptor);

        final String defaultNamespace = portletApplicationDescriptor.getDefaultNamespace();

        // No support found so far, do more complex namespace matching
        final PortletDefinition portletDescriptor =
                this.portletDefinitionRegistry.getParentPortletDescriptor(portletDefinitionId);
        if (portletDescriptor == null) {
            return false;
        }

        final List<? extends EventDefinitionReference> supportedProcessingEvents =
                portletDescriptor.getSupportedProcessingEvents();
        for (final EventDefinitionReference eventDefinitionReference : supportedProcessingEvents) {
            final QName qualifiedName = eventDefinitionReference.getQualifiedName(defaultNamespace);
            if (qualifiedName == null) {
                continue;
            }

            // See if the supported qname and event qname match explicitly
            // Look for alias names
            if (qualifiedName.equals(eventName) || aliases.contains(qualifiedName)) {
                this.supportedEventCache.put(new Element(key, Boolean.TRUE));
                return true;
            }

            // Look for namespaced events
            if (StringUtils.isEmpty(qualifiedName.getNamespaceURI())) {
                final QName namespacedName =
                        new QName(defaultNamespace, qualifiedName.getLocalPart());
                if (eventName.equals(namespacedName)) {
                    this.supportedEventCache.put(new Element(key, Boolean.TRUE));
                    return true;
                }
            }
        }

        this.supportedEventCache.put(new Element(key, Boolean.FALSE));
        return false;
    }
}
