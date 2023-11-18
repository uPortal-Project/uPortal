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
package org.apereo.portal.io.xml.portlet;

import java.math.BigInteger;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import org.apache.commons.lang3.StringUtils;
import org.apereo.portal.IUserIdentityStore;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.dao.jpa.PortletDefinitionImpl;
import org.apereo.portal.portlet.dao.jpa.PortletDefinitionParameterImpl;
import org.apereo.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.IPortletDescriptorKey;
import org.apereo.portal.portlet.om.IPortletLifecycleEntry;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.om.IPortletType;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.apereo.portal.portlet.registry.IPortletCategoryRegistry;
import org.apereo.portal.portlet.registry.IPortletTypeRegistry;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.PersonFactory;
import org.apereo.portal.xml.PortletDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Responsible for taking an {@link ExternalPortletDefinition} and producing an {@link
 * IPortletDefinition}.
 */
@Component
/* package-private */ class ExternalPortletDefinitionUnmarshaller {

    /**
     * Represents a special datetime value that helps us solve a problem we have with supporting
     * older portlet-definition entities in MAINTENANCE mode. We used to handle MAINTENANCE with a
     * parameter; now it is a first class lifecycle state in the database and in XML. Lifecycle
     * states require a data, but the older, parameter-based MAINTENANCE model doesn't include one.
     * We will use the current instant in these cases, but we need a way for the XML to signal that
     * we should do so. According to the XSD, the datetime value cannot be empty in any way. This
     * datetime is the last second before the present millennium -- more than 10 years before
     * MAINTENANCE mode was implemented. When we see this datetime, we will substitute the present
     * datetime.
     */
    /* package-private */ static final String USE_CURRENT_DATETIME_SIGNAL_DATE =
            "1999-12-31T23:59:59.000Z";

    @Value("${org.apereo.portal.io.errorOnChannel:false}")
    private boolean errorOnChannel;

    @Autowired private IPortletTypeRegistry portletTypeRegistry;

    @Autowired private IPortletCategoryRegistry portletCategoryRegistry;

    @Autowired private IPortletDefinitionDao portletDefinitionDao;

    @Autowired private IUserIdentityStore userIdentityStore;

    private final Date useCurrentDatetimeSignal;

    private final IPerson systemUser = PersonFactory.createSystemPerson();

    private final Logger logger = LoggerFactory.getLogger(getClass());

    public ExternalPortletDefinitionUnmarshaller() {
        final DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
        try {
            useCurrentDatetimeSignal = dateFormat.parse(USE_CURRENT_DATETIME_SIGNAL_DATE);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    /** For unit tests. */
    /* package-private */ void setUserIdentityStore(IUserIdentityStore userIdentityStore) {
        this.userIdentityStore = userIdentityStore;
    }

    /* package-private */ IPortletDefinition unmarshall(ExternalPortletDefinition epd) {

        final PortletDescriptor portletDescriptor = epd.getPortletDescriptor();
        final Boolean isFramework = portletDescriptor.isIsFramework();

        if (isFramework != null
                && isFramework
                && "UPGRADED_CHANNEL_IS_NOT_A_PORTLET".equals(portletDescriptor.getPortletName())) {
            if (errorOnChannel) {
                throw new IllegalArgumentException(
                        epd.getFname()
                                + " is not a portlet. It was likely an IChannel from a previous version of uPortal and cannot be imported.");
            }

            logger.warn(
                    epd.getFname()
                            + " is not a portlet. It was likely an IChannel from a previous version of uPortal and will not be imported.");
            return null;
        }

        // get the portlet type
        final IPortletType portletType = portletTypeRegistry.getPortletType(epd.getType());
        if (portletType == null) {
            throw new IllegalArgumentException("No portlet type registered for: " + epd.getType());
        }

        final String fname = epd.getFname();
        IPortletDefinition result = portletDefinitionDao.getPortletDefinitionByFname(fname);
        if (result == null) {
            result =
                    new PortletDefinitionImpl(
                            portletType,
                            fname,
                            epd.getName(),
                            epd.getTitle(),
                            portletDescriptor.getWebAppName(),
                            portletDescriptor.getPortletName(),
                            isFramework != null ? isFramework : false);
        } else {
            final IPortletDescriptorKey portletDescriptorKey = result.getPortletDescriptorKey();
            portletDescriptorKey.setPortletName(portletDescriptor.getPortletName());
            if (isFramework != null && isFramework) {
                portletDescriptorKey.setFrameworkPortlet(true);
                portletDescriptorKey.setWebAppName(null);
            } else {
                portletDescriptorKey.setFrameworkPortlet(false);
                portletDescriptorKey.setWebAppName(portletDescriptor.getWebAppName());
            }
            result.setName(epd.getName());
            result.setTitle(epd.getTitle());
            result.setType(portletType);
        }

        result.setDescription(epd.getDesc());
        final BigInteger timeout = epd.getTimeout();
        if (timeout != null) {
            result.setTimeout(timeout.intValue());
        }
        final BigInteger actionTimeout = epd.getActionTimeout();
        if (actionTimeout != null) {
            result.setActionTimeout(actionTimeout.intValue());
        }
        final BigInteger eventTimeout = epd.getEventTimeout();
        if (eventTimeout != null) {
            result.setEventTimeout(eventTimeout.intValue());
        }
        final BigInteger renderTimeout = epd.getRenderTimeout();
        if (renderTimeout != null) {
            result.setRenderTimeout(renderTimeout.intValue());
        }
        final BigInteger resourceTimeout = epd.getResourceTimeout();
        if (resourceTimeout != null) {
            result.setResourceTimeout(resourceTimeout.intValue());
        }

        unmarshallLifecycle(epd.getLifecycle(), result);

        final Set<IPortletDefinitionParameter> parameters = new LinkedHashSet<>();
        for (ExternalPortletParameter param : epd.getParameters()) {
            parameters.add(new PortletDefinitionParameterImpl(param.getName(), param.getValue()));
        }
        result.setParameters(parameters);

        final ArrayList<IPortletPreference> preferenceList = new ArrayList<>();
        for (ExternalPortletPreference pref : epd.getPortletPreferences()) {
            final List<String> valueList = pref.getValues();
            final String[] values = valueList.toArray(new String[valueList.size()]);

            final Boolean readOnly = pref.isReadOnly();
            preferenceList.add(
                    new PortletPreferenceImpl(
                            pref.getName(), readOnly != null ? readOnly : false, values));
        }
        result.setPortletPreferences(preferenceList);

        return result;
    }

    /* package-private */ void unmarshallLifecycle(
            final Lifecycle lifecycle, final IPortletDefinition portletDefinition) {

        /*
         * If this is an existing portletDefinition, it may (probably does) already contain
         * lifecycle entries.  We need to remove those, because the lifecycle of a portlet after
         * import should reflect what the document says exactly.
         */
        portletDefinition.clearLifecycle();

        if (lifecycle == null) {
            /*
             * For backwards-compatibility, a complete absence of
             * lifecycle information means the portlet is published.
             */
            portletDefinition.updateLifecycleState(PortletLifecycleState.PUBLISHED, systemUser);
        } else if (lifecycle.getEntries().isEmpty()) {
            /*
             * According to the comments for 4.3, we're supposed
             * to leave the portlet in CREATED state.
             */
            portletDefinition.updateLifecycleState(PortletLifecycleState.CREATED, systemUser);
        } else {
            /*
             * Use a TreeMap because we need to be certain the the entries
             * get applied to the new portlet definition in a sane order...
             */
            Map<IPortletLifecycleEntry, IPerson> convertedEntries = new TreeMap<>();
            /*
             * Convert each LifecycleEntry (JAXB) to an IPortletLifecycleEntry (internal)
             */
            for (LifecycleEntry entry : lifecycle.getEntries()) {
                final IPerson user =
                        StringUtils.isNotBlank(entry.getUser())
                                ? userIdentityStore.getPerson(entry.getUser(), true)
                                : systemUser; // default
                // We will support case insensitivity of entry/@name in the XML
                final PortletLifecycleState state =
                        PortletLifecycleState.valueOf(entry.getName().toUpperCase());
                // Entries added by an upgrade transform will not have a date
                final Date date =
                        entry.getValue().equals(useCurrentDatetimeSignal)
                                ? new Date()
                                : entry.getValue().getTime();
                convertedEntries.put(
                        new IPortletLifecycleEntry() {
                            @Override
                            public int getUserId() {
                                return user.getID();
                            }

                            @Override
                            public PortletLifecycleState getLifecycleState() {
                                return state;
                            }

                            @Override
                            public Date getDate() {
                                return date;
                            }

                            @Override
                            public int compareTo(IPortletLifecycleEntry o) {
                                int result = date.compareTo(o.getDate());
                                if (result == 0) {
                                    result = state.getOrder() - o.getLifecycleState().getOrder();
                                }
                                return result;
                            }
                        },
                        user);
            }
            /*
             * Apply them to the portlet definition
             */
            convertedEntries.forEach(
                    (k, v) -> {
                        portletDefinition.updateLifecycleState(
                                k.getLifecycleState(), v, k.getDate());
                    });
        }
    }
}
