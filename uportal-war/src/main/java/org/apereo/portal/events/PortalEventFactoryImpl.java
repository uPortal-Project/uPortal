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
package org.apereo.portal.events;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.namespace.QName;
import org.apache.commons.lang.StringUtils;
import org.apereo.portal.IPortalInfoProvider;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletWindow;
import org.apereo.portal.portlet.om.IPortletWindowId;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.portlet.rendering.worker.IPortletExecutionWorker;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.SystemPerson;
import org.apereo.portal.services.GroupService;
import org.apereo.portal.tenants.ITenant;
import org.apereo.portal.url.IPortalRequestInfo;
import org.apereo.portal.url.IPortalRequestUtils;
import org.apereo.portal.url.IPortletRequestInfo;
import org.apereo.portal.url.IUrlSyntaxProvider;
import org.apereo.portal.url.ParameterMap;
import org.apereo.portal.utils.IncludeExcludeUtils;
import org.apereo.portal.utils.RandomTokenGenerator;
import org.apereo.portal.utils.SerializableObject;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import org.springframework.stereotype.Service;
import org.springframework.web.util.WebUtils;

@Service("portalEventFactory")
public class PortalEventFactoryImpl implements IPortalEventFactory, ApplicationEventPublisherAware {
    private static final String EVENT_SESSION_MUTEX =
            PortalEventFactoryImpl.class.getName() + ".EVENT_SESSION_MUTEX";
    private static final String EVENT_SESSION_ID_ATTR =
            PortalEventFactoryImpl.class.getName() + ".EVENT_SESSION_ID_ATTR";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final AtomicReference<String> systemSessionId = new AtomicReference<String>();

    private int maxParameters = 50;
    private int maxParameterLength = 500;
    private Set<String> groupIncludes = Collections.emptySet();
    private Set<String> groupExcludes = Collections.emptySet();
    private Set<String> attributeIncludes = Collections.emptySet();
    private Set<String> attributeExcludes = Collections.emptySet();
    private IPersonAttributeDao personAttributeDao;
    private IPortalInfoProvider portalInfoProvider;
    private IPortalRequestUtils portalRequestUtils;
    private IPersonManager personManager;
    private ApplicationEventPublisher applicationEventPublisher;
    private IPortletWindowRegistry portletWindowRegistry;
    private IUrlSyntaxProvider urlSyntaxProvider;

    /**
     * Maximum number of parameters to allow in an event, also used to limit the number of values
     * for multi-valued parameters.
     *
     * <p>Defaults to 50
     */
    public void setMaxParameters(int maxParameters) {
        this.maxParameters = maxParameters;
    }

    /**
     * Maximum length of parameter names or values.
     *
     * <p>Defaults to 500 characters.
     */
    public void setMaxParameterLength(int maxParameterLength) {
        this.maxParameterLength = maxParameterLength;
    }

    /**
     * Set of groups to be explicitly included in the {@link LoginEvent}
     *
     * @see IncludeExcludeUtils#included(Object, java.util.Collection, java.util.Collection)
     */
    public void setGroupIncludes(Set<String> groupIncludes) {
        this.groupIncludes = groupIncludes;
    }

    /**
     * Set of groups to be explicitly excluded in the {@link LoginEvent}
     *
     * @see IncludeExcludeUtils#included(Object, java.util.Collection, java.util.Collection)
     */
    public void setGroupExcludes(Set<String> groupExcludes) {
        this.groupExcludes = groupExcludes;
    }

    /**
     * Set of attributes to be explicitly included in the {@link LoginEvent}
     *
     * @see IncludeExcludeUtils#included(Object, java.util.Collection, java.util.Collection)
     */
    public void setAttributeIncludes(Set<String> attributeIncludes) {
        this.attributeIncludes = attributeIncludes;
    }

    /**
     * Set of attributes to be explicitly excluded in the {@link LoginEvent}
     *
     * @see IncludeExcludeUtils#included(Object, java.util.Collection, java.util.Collection)
     */
    public void setAttributeExcludes(Set<String> attributeExcludes) {
        this.attributeExcludes = attributeExcludes;
    }

    @Autowired
    public void setPortalInfoProvider(IPortalInfoProvider portalInfoProvider) {
        this.portalInfoProvider = portalInfoProvider;
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Autowired
    public void setPersonAttributeDao(
            @Qualifier("personAttributeDao") IPersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }

    @Autowired
    public void setPortalRequestUtils(IPortalRequestUtils portalRequestUtils) {
        this.portalRequestUtils = portalRequestUtils;
    }

    @Autowired
    public void setPersonManager(IPersonManager personManager) {
        this.personManager = personManager;
    }

    @Autowired
    public void setPortletWindowRegistry(IPortletWindowRegistry portletWindowRegistry) {
        this.portletWindowRegistry = portletWindowRegistry;
    }

    @Autowired
    public void setUrlSyntaxProvider(IUrlSyntaxProvider urlSyntaxProvider) {
        this.urlSyntaxProvider = urlSyntaxProvider;
    }

    @Override
    public void publishLoginEvent(HttpServletRequest request, Object source, IPerson person) {
        final PortalEvent.PortalEventBuilder portalEventBuilder =
                this.createPortalEventBuilder(source, person, request);

        final Set<String> groups = this.getGroupsForUser(person);
        final Map<String, List<String>> attributes = this.getAttributesForUser(person);

        final LoginEvent loginEvent = new LoginEvent(portalEventBuilder, groups, attributes);
        this.applicationEventPublisher.publishEvent(loginEvent);
    }

    @Override
    public void publishLogoutEvent(HttpServletRequest request, Object source, IPerson person) {
        final PortalEvent.PortalEventBuilder portalEventBuilder =
                this.createPortalEventBuilder(source, person, request);

        final LogoutEvent logoutEvent = new LogoutEvent(portalEventBuilder);
        this.applicationEventPublisher.publishEvent(logoutEvent);
    }

    @Override
    public void publishPortletAddedToLayoutPortalEvent(
            HttpServletRequest request,
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String parentFolderId,
            String fname) {
        final PortalEvent.PortalEventBuilder portalEventBuilder =
                this.createPortalEventBuilder(source, request);

        final PortletAddedToLayoutPortalEvent portletAddedToLayoutPortalEvent =
                new PortletAddedToLayoutPortalEvent(
                        portalEventBuilder, layoutOwner, layoutId, parentFolderId, fname);
        this.applicationEventPublisher.publishEvent(portletAddedToLayoutPortalEvent);
    }

    @Override
    public void publishPortletAddedToLayoutPortalEvent(
            Object source, IPerson person, long layoutId, String parentFolderId, String fname) {
        this.publishPortletAddedToLayoutPortalEvent(
                null, source, person, layoutId, parentFolderId, fname);
    }

    @Override
    public void publishPortletMovedInLayoutPortalEvent(
            HttpServletRequest request,
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String newParentFolderId,
            String fname) {
        final PortalEvent.PortalEventBuilder portalEventBuilder =
                this.createPortalEventBuilder(source, request);

        final PortletMovedInLayoutPortalEvent portletMovedInLayoutPortalEvent =
                new PortletMovedInLayoutPortalEvent(
                        portalEventBuilder,
                        layoutOwner,
                        layoutId,
                        oldParentFolderId,
                        newParentFolderId,
                        fname);
        this.applicationEventPublisher.publishEvent(portletMovedInLayoutPortalEvent);
    }

    @Override
    public void publishPortletMovedInLayoutPortalEvent(
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String newParentFolderId,
            String fname) {
        this.publishPortletMovedInLayoutPortalEvent(
                null, source, layoutOwner, layoutId, oldParentFolderId, newParentFolderId, fname);
    }

    @Override
    public void publishPortletDeletedFromLayoutPortalEvent(
            HttpServletRequest request,
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String fname) {
        final PortalEvent.PortalEventBuilder portalEventBuilder =
                this.createPortalEventBuilder(source, request);

        final PortletDeletedFromLayoutPortalEvent portletDeletedFromLayoutPortalEvent =
                new PortletDeletedFromLayoutPortalEvent(
                        portalEventBuilder, layoutOwner, layoutId, oldParentFolderId, fname);
        this.applicationEventPublisher.publishEvent(portletDeletedFromLayoutPortalEvent);
    }

    @Override
    public void publishPortletDeletedFromLayoutPortalEvent(
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String fname) {
        this.publishPortletDeletedFromLayoutPortalEvent(
                null, source, layoutOwner, layoutId, oldParentFolderId, fname);
    }

    @Override
    public void publishFolderAddedToLayoutPortalEvent(
            HttpServletRequest request,
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String newFolderId) {
        final PortalEvent.PortalEventBuilder portalEventBuilder =
                this.createPortalEventBuilder(source, request);

        final FolderAddedToLayoutPortalEvent folderAddedToLayoutPortalEvent =
                new FolderAddedToLayoutPortalEvent(
                        portalEventBuilder, layoutOwner, layoutId, newFolderId);
        this.applicationEventPublisher.publishEvent(folderAddedToLayoutPortalEvent);
    }

    @Override
    public void publishFolderAddedToLayoutPortalEvent(
            Object source, IPerson layoutOwner, long layoutId, String newFolderId) {
        this.publishFolderAddedToLayoutPortalEvent(
                null, source, layoutOwner, layoutId, newFolderId);
    }

    @Override
    public void publishFolderMovedInLayoutPortalEvent(
            HttpServletRequest request,
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String movedFolderId) {
        final PortalEvent.PortalEventBuilder portalEventBuilder =
                this.createPortalEventBuilder(source, request);

        final FolderMovedInLayoutPortalEvent folderMovedInLayoutPortalEvent =
                new FolderMovedInLayoutPortalEvent(
                        portalEventBuilder,
                        layoutOwner,
                        layoutId,
                        oldParentFolderId,
                        movedFolderId);
        this.applicationEventPublisher.publishEvent(folderMovedInLayoutPortalEvent);
    }

    @Override
    public void publishFolderMovedInLayoutPortalEvent(
            Object source,
            IPerson person,
            long layoutId,
            String oldParentFolderId,
            String movedFolderId) {
        this.publishFolderMovedInLayoutPortalEvent(
                null, source, person, layoutId, oldParentFolderId, movedFolderId);
    }

    @Override
    public void publishFolderDeletedFromLayoutPortalEvent(
            HttpServletRequest request,
            Object source,
            IPerson layoutOwner,
            long layoutId,
            String oldParentFolderId,
            String deletedFolderId,
            String deletedFolderName) {
        final PortalEvent.PortalEventBuilder portalEventBuilder =
                this.createPortalEventBuilder(source, request);

        final FolderDeletedFromLayoutPortalEvent folderDeletedFromLayoutPortalEvent =
                new FolderDeletedFromLayoutPortalEvent(
                        portalEventBuilder,
                        layoutOwner,
                        layoutId,
                        oldParentFolderId,
                        deletedFolderId,
                        deletedFolderName);
        this.applicationEventPublisher.publishEvent(folderDeletedFromLayoutPortalEvent);
    }

    @Override
    public void publishFolderDeletedFromLayoutPortalEvent(
            Object source,
            IPerson person,
            long layoutId,
            String oldParentFolderId,
            String deletedFolderId,
            String deletedFolderName) {
        this.publishFolderDeletedFromLayoutPortalEvent(
                null,
                source,
                person,
                layoutId,
                oldParentFolderId,
                deletedFolderId,
                deletedFolderName);
    }

    @Override
    public void publishPortletHungEvent(
            HttpServletRequest request, Object source, IPortletExecutionWorker<?> worker) {
        final PortalEvent.PortalEventBuilder eventBuilder =
                this.createPortalEventBuilder(source, request);
        final PortletHungEvent portletHungEvent = new PortletHungEvent(eventBuilder, worker);
        this.applicationEventPublisher.publishEvent(portletHungEvent);
    }

    @Override
    public void publishPortletHungCompleteEvent(Object source, IPortletExecutionWorker<?> worker) {
        final PortalEvent.PortalEventBuilder eventBuilder =
                this.createPortalEventBuilder(source, null);
        final PortletHungCompleteEvent portletHungCompleteEvent =
                new PortletHungCompleteEvent(eventBuilder, worker);
        this.applicationEventPublisher.publishEvent(portletHungCompleteEvent);
    }

    @Override
    public void publishPortletActionExecutionEvent(
            HttpServletRequest request,
            Object source,
            IPortletWindowId portletWindowId,
            long executionTime) {
        final PortalEvent.PortalEventBuilder eventBuilder =
                this.createPortalEventBuilder(source, request);
        final PortletExecutionEvent.PortletExecutionEventBuilder portletEventBuilder =
                this.createPortletExecutionEventBuilder(
                        eventBuilder, portletWindowId, executionTime, false);

        final PortletActionExecutionEvent portletActionExecutionEvent =
                new PortletActionExecutionEvent(portletEventBuilder);
        this.applicationEventPublisher.publishEvent(portletActionExecutionEvent);
    }

    @Override
    public void publishPortletEventExecutionEvent(
            HttpServletRequest request,
            Object source,
            IPortletWindowId portletWindowId,
            long executionTime,
            QName eventName) {

        final PortalEvent.PortalEventBuilder eventBuilder =
                this.createPortalEventBuilder(source, request);
        final PortletExecutionEvent.PortletExecutionEventBuilder portletEventBuilder =
                this.createPortletExecutionEventBuilder(
                        eventBuilder, portletWindowId, executionTime, false);

        final PortletEventExecutionEvent portletEventExecutionEvent =
                new PortletEventExecutionEvent(portletEventBuilder, eventName);
        this.applicationEventPublisher.publishEvent(portletEventExecutionEvent);
    }

    @Override
    public void publishPortletRenderHeaderExecutionEvent(
            HttpServletRequest request,
            Object source,
            IPortletWindowId portletWindowId,
            long executionTime,
            boolean targeted,
            boolean cached) {

        final PortalEvent.PortalEventBuilder eventBuilder =
                this.createPortalEventBuilder(source, request);
        final PortletExecutionEvent.PortletExecutionEventBuilder portletEventBuilder =
                this.createPortletExecutionEventBuilder(
                        eventBuilder, portletWindowId, executionTime, false);

        final PortletRenderHeaderExecutionEvent portletRenderHeaderExecutionEvent =
                new PortletRenderHeaderExecutionEvent(portletEventBuilder, targeted, cached);
        this.applicationEventPublisher.publishEvent(portletRenderHeaderExecutionEvent);
    }

    @Override
    public void publishPortletRenderExecutionEvent(
            HttpServletRequest request,
            Object source,
            IPortletWindowId portletWindowId,
            long executionTime,
            boolean targeted,
            boolean cached) {

        final PortalEvent.PortalEventBuilder eventBuilder =
                this.createPortalEventBuilder(source, request);
        final PortletExecutionEvent.PortletExecutionEventBuilder portletEventBuilder =
                this.createPortletExecutionEventBuilder(
                        eventBuilder, portletWindowId, executionTime, false);

        final PortletRenderExecutionEvent portletRenderExecutionEvent =
                new PortletRenderExecutionEvent(portletEventBuilder, targeted, cached);
        this.applicationEventPublisher.publishEvent(portletRenderExecutionEvent);
    }

    @Override
    public void publishPortletResourceExecutionEvent(
            HttpServletRequest request,
            Object source,
            IPortletWindowId portletWindowId,
            long executionTime,
            boolean usedBrowserCache,
            boolean usedPortalCache) {

        final PortalEvent.PortalEventBuilder eventBuilder =
                this.createPortalEventBuilder(source, request);
        final PortletExecutionEvent.PortletExecutionEventBuilder portletEventBuilder =
                this.createPortletExecutionEventBuilder(
                        eventBuilder, portletWindowId, executionTime, false);

        //Get the resource Id
        final IPortalRequestInfo portalRequestInfo =
                this.urlSyntaxProvider.getPortalRequestInfo(request);
        final String resourceId = getResourceId(portletWindowId, portalRequestInfo);

        final PortletResourceExecutionEvent portletResourceExecutionEvent =
                new PortletResourceExecutionEvent(
                        portletEventBuilder, resourceId, usedBrowserCache, usedPortalCache);
        this.applicationEventPublisher.publishEvent(portletResourceExecutionEvent);
    }

    @Override
    public void publishPortalRenderEvent(
            HttpServletRequest request,
            Object source,
            String requestPathInfo,
            long executionTimeNano,
            IPortalRequestInfo portalRequestInfo) {
        final PortalEvent.PortalEventBuilder eventBuilder =
                this.createPortalEventBuilder(source, request);

        final Map<String, List<String>> portalParameters =
                this.pruneParameters(portalRequestInfo.getPortalParameters());
        final PortalRenderEvent portalRenderEvent =
                new PortalRenderEvent(
                        eventBuilder,
                        requestPathInfo,
                        executionTimeNano,
                        portalRequestInfo.getUrlState(),
                        portalRequestInfo.getUrlType(),
                        portalParameters,
                        portalRequestInfo.getTargetedLayoutNodeId());

        this.applicationEventPublisher.publishEvent(portalRenderEvent);
    }

    /*
     * Tenant Events
     */

    @Override
    public void publishTenantCreatedTenantEvent(
            HttpServletRequest request, Object source, ITenant tenant) {
        final PortalEvent.PortalEventBuilder eventBuilder =
                this.createPortalEventBuilder(source, request);
        final TenantCreatedTenantEvent event = new TenantCreatedTenantEvent(eventBuilder, tenant);
        this.applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishTenantUpdatedTenantEvent(
            HttpServletRequest request, Object source, ITenant tenant) {
        final PortalEvent.PortalEventBuilder eventBuilder =
                this.createPortalEventBuilder(source, request);
        final TenantUpdatedTenantEvent event = new TenantUpdatedTenantEvent(eventBuilder, tenant);
        this.applicationEventPublisher.publishEvent(event);
    }

    @Override
    public void publishTenantRemovedTenantEvent(
            HttpServletRequest request, Object source, ITenant tenant) {
        final PortalEvent.PortalEventBuilder eventBuilder =
                this.createPortalEventBuilder(source, request);
        final TenantRemovedTenantEvent event = new TenantRemovedTenantEvent(eventBuilder, tenant);
        this.applicationEventPublisher.publishEvent(event);
    }

    /*
     * Implementation
     */

    protected PortalEvent.PortalEventBuilder createPortalEventBuilder(
            Object source, HttpServletRequest request) {
        request = getCurrentPortalRequest(request);
        final IPerson person = this.getPerson(request);
        return this.createPortalEventBuilder(source, person, request);
    }

    protected PortalEvent.PortalEventBuilder createPortalEventBuilder(
            Object source, IPerson person, HttpServletRequest request) {
        final String serverName = this.portalInfoProvider.getServerName();
        final String eventSessionId = this.getPortalEventSessionId(request, person);
        request = getCurrentPortalRequest(request);
        return new PortalEvent.PortalEventBuilder(
                source, serverName, eventSessionId, person, request);
    }

    protected PortletExecutionEvent.PortletExecutionEventBuilder createPortletExecutionEventBuilder(
            PortalEvent.PortalEventBuilder portalEventBuilder,
            IPortletWindowId portletWindowId,
            long executionTimeNano,
            boolean renderRequest) {

        final HttpServletRequest portalRequest = portalEventBuilder.getPortalRequest();

        //Get the portlet's fname
        final IPortletWindow portletWindow =
                this.portletWindowRegistry.getPortletWindow(portalRequest, portletWindowId);
        final IPortletEntity portletEntity = portletWindow.getPortletEntity();
        final IPortletDefinition portletDefinition = portletEntity.getPortletDefinition();
        final String fname = portletDefinition.getFName();

        //Get the parameters used for the portlet execution
        final Map<String, List<String>> parameters =
                getParameters(portalRequest, portletWindowId, renderRequest);

        //Get the state & mode used for this request
        final WindowState windowState = portletWindow.getWindowState();
        final PortletMode portletMode = portletWindow.getPortletMode();

        return new PortletExecutionEvent.PortletExecutionEventBuilder(
                portalEventBuilder,
                portletWindowId,
                fname,
                executionTimeNano,
                parameters,
                windowState,
                portletMode);
    }

    /** The portlet resource request resourceId */
    protected String getResourceId(
            IPortletWindowId portletWindowId, final IPortalRequestInfo portalRequestInfo) {
        final IPortletRequestInfo portletRequestInfo =
                portalRequestInfo.getPortletRequestInfo(portletWindowId);
        if (portletRequestInfo == null) {
            return null;
        }

        return portletRequestInfo.getResourceId();
    }

    protected Map<String, List<String>> getParameters(
            HttpServletRequest httpServletRequest,
            IPortletWindowId portletWindowId,
            boolean renderRequest) {
        final IPortalRequestInfo portalRequestInfo =
                this.urlSyntaxProvider.getPortalRequestInfo(httpServletRequest);
        final IPortletRequestInfo portletRequestInfo =
                portalRequestInfo.getPortletRequestInfo(portletWindowId);

        if (portletRequestInfo != null) {
            final Map<String, List<String>> parameters = portletRequestInfo.getPortletParameters();
            return pruneParameters(parameters);
        }

        //Only re-use render parameters on a render request
        if (renderRequest) {
            final IPortletWindow portletWindow =
                    this.portletWindowRegistry.getPortletWindow(
                            httpServletRequest, portletWindowId);
            final Map<String, String[]> parameters = portletWindow.getRenderParameters();
            return pruneParameters(ParameterMap.immutableCopyOfArrayMap(parameters));
        }

        return Collections.emptyMap();
    }

    protected HttpServletRequest getCurrentPortalRequest(HttpServletRequest request) {
        if (request == null) {
            try {
                return portalRequestUtils.getCurrentPortalRequest();
            } catch (IllegalStateException e) {
                return null;
            }
        }

        return portalRequestUtils.getOriginalPortalRequest(request);
    }

    protected IPerson getPerson(HttpServletRequest request) {
        if (request == null) {
            return SystemPerson.INSTANCE;
        }

        return this.personManager.getPerson(request);
    }

    @Override
    public String getPortalEventSessionId(HttpServletRequest request, IPerson person) {
        if (request == null) {
            try {
                request = this.portalRequestUtils.getCurrentPortalRequest();
            } catch (IllegalStateException e) {
                //System person is immutable, track their session id locally
                if (person == SystemPerson.INSTANCE) {
                    String sessionId = this.systemSessionId.get();
                    if (sessionId == null) {
                        sessionId = createSessionId(person);
                        if (!systemSessionId.compareAndSet(null, sessionId)) {
                            //Another thread beat us to CaS, grab the set value
                            sessionId = systemSessionId.get();
                        }
                    }
                    return sessionId;
                }

                //Try tracking sessionId in person object directly
                synchronized (person) {
                    String sessionId = (String) person.getAttribute(EVENT_SESSION_ID_ATTR);
                    if (sessionId == null) {
                        sessionId = createSessionId(person);
                        person.setAttribute(EVENT_SESSION_ID_ATTR, sessionId);
                    }

                    return sessionId;
                }
            }
        }

        final HttpSession session = request.getSession();

        //Need to sync on session scoped object to ensure only one id exists per HttpSession
        final Object eventSessionMutex = this.getEventSessionMutex(session);
        synchronized (eventSessionMutex) {
            String eventSessionId = (String) session.getAttribute(EVENT_SESSION_ID_ATTR);
            if (eventSessionId != null) {
                return eventSessionId;
            }

            eventSessionId = createSessionId(person);
            session.setAttribute(EVENT_SESSION_ID_ATTR, eventSessionId);

            this.logger.debug("Generated PortalEvent SessionId: {}", eventSessionId);

            return eventSessionId;
        }
    }

    /** Creates an event session id for the person */
    protected String createSessionId(IPerson person) {
        return RandomTokenGenerator.INSTANCE.generateRandomToken(8);
    }

    protected Set<String> getGroupsForUser(IPerson person) {
        final IGroupMember member = GroupService.getGroupMember(person.getEntityIdentifier());

        final Set<String> groupKeys = new LinkedHashSet<>();
        for (IGroupMember group : member.getAncestorGroups()) {
            final String groupKey = group.getKey();

            if (IncludeExcludeUtils.included(groupKey, this.groupIncludes, this.groupExcludes)) {
                groupKeys.add(groupKey);
            }
        }

        return groupKeys;
    }

    protected Map<String, List<String>> getAttributesForUser(IPerson person) {
        final IPersonAttributes personAttributes =
                this.personAttributeDao.getPerson(person.getUserName());

        final Map<String, List<String>> attributes = new LinkedHashMap<String, List<String>>();

        for (final Map.Entry<String, List<Object>> attributeEntry :
                personAttributes.getAttributes().entrySet()) {
            final String attributeName = attributeEntry.getKey();
            final List<Object> values = attributeEntry.getValue();

            if (IncludeExcludeUtils.included(
                    attributeName, this.attributeIncludes, this.attributeExcludes)) {
                final List<String> stringValues =
                        new ArrayList<String>(values == null ? 0 : values.size());

                if (values != null) {
                    for (final Object value : values) {
                        if (value instanceof CharSequence
                                || value instanceof Number
                                || value instanceof Date
                                || value instanceof Calendar) {
                            stringValues.add(value.toString());
                        }
                    }
                }

                attributes.put(attributeName, stringValues);
            }
        }

        return attributes;
    }

    /** Get a session scoped mutex specific to this class */
    protected final Object getEventSessionMutex(HttpSession session) {
        synchronized (WebUtils.getSessionMutex(session)) {
            SerializableObject mutex =
                    (SerializableObject) session.getAttribute(EVENT_SESSION_MUTEX);
            if (mutex == null) {
                mutex = new SerializableObject();
                session.setAttribute(EVENT_SESSION_MUTEX, mutex);
            }

            return mutex;
        }
    }

    protected final Map<String, List<String>> pruneParameters(
            Map<String, List<String>> parameters) {
        final Builder<String, List<String>> builder = ImmutableMap.builder();

        int paramCount = 0;
        for (final Map.Entry<String, List<String>> parameterEntry : parameters.entrySet()) {
            if (paramCount == this.maxParameters) {
                break;
            }
            paramCount++;

            final String name = StringUtils.left(parameterEntry.getKey(), this.maxParameterLength);
            List<String> values = parameterEntry.getValue();
            if (values == null) {
                values = Collections.emptyList();
            }

            final com.google.common.collect.ImmutableList.Builder<String> valuesBuilder =
                    ImmutableList.builder();

            int valueCount = 0;
            for (final String value : values) {
                if (valueCount == this.maxParameters) {
                    break;
                }
                valueCount++;

                valuesBuilder.add(StringUtils.left(value, this.maxParameterLength));
            }

            builder.put(name, valuesBuilder.build());
        }

        return builder.build();
    }
}
