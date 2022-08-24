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
package org.apereo.portal.portlets.portletadmin;

import java.io.Serializable;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.FastDateFormat;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.IPortletLifecycleEntry;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.apereo.portal.portletpublishing.xml.MultiValuedPreferenceInputType;
import org.apereo.portal.portletpublishing.xml.Parameter;
import org.apereo.portal.portletpublishing.xml.ParameterInputType;
import org.apereo.portal.portletpublishing.xml.PortletPublishingDefinition;
import org.apereo.portal.portletpublishing.xml.Preference;
import org.apereo.portal.portletpublishing.xml.PreferenceInputType;
import org.apereo.portal.portletpublishing.xml.SingleValuedPreferenceInputType;
import org.apereo.portal.portletpublishing.xml.Step;
import org.apereo.portal.portlets.Attribute;
import org.apereo.portal.portlets.AttributeFactory;
import org.apereo.portal.portlets.BooleanAttribute;
import org.apereo.portal.portlets.BooleanAttributeFactory;
import org.apereo.portal.portlets.StringListAttribute;
import org.apereo.portal.portlets.StringListAttributeFactory;
import org.apereo.portal.xml.PortletDescriptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PortletDefinitionForm implements Serializable {

    private static final String FRAMEWORK_PORTLET_URL = "/uPortal";
    private static final FastDateFormat dateFormat = FastDateFormat.getInstance("M/d/yyyy");

    private static final long serialVersionUID = 892741367149099649L;
    protected final transient Log log = LogFactory.getLog(getClass());

    /** Main portlet fields */
    private String id = null;

    private String fname = "";
    private String name = "";
    private String description = "";
    private String title = "";
    private String applicationId = "";
    private String portletName = "";
    private boolean framework = false;
    private int timeout = 5000;
    private int typeId;

    /** Lifecycle information */
    private PortletLifecycleState lifecycleState = PortletLifecycleState.CREATED;

    private Date publishDate;
    private int publishHour = 12;
    private int publishMinute = 0;
    private int publishAmPm = 0;
    private Date expirationDate;
    private int expirationHour = 12;
    private int expirationMinute = 0;
    private int expirationAmPm = 0;
    private String customMaintenanceMessage;

    /** Maintenance Scheduler information */
    private boolean stopImmediately;
    private String stopDate;
    private String stopTime;
    private boolean restartManually;
    private String restartDate;
    private String restartTime;

    /** Portlet controls */
    private boolean editable;

    private boolean hasHelp;
    private boolean hasAbout;
    private boolean configurable;

    /** Principals and categories */
    private SortedSet<JsonEntityBean> principals = new TreeSet<>();

    private SortedSet<JsonEntityBean> categories = new TreeSet<>();
    private Set<String> permissions = new HashSet<>();

    /** Parameters and preferences */
    @SuppressWarnings("unchecked")
    private Map<String, Attribute> parameters =
            LazyMap.decorate(new HashMap<String, Attribute>(), new AttributeFactory());

    @SuppressWarnings("unchecked")
    private Map<String, StringListAttribute> portletPreferences =
            LazyMap.decorate(
                    new HashMap<String, StringListAttribute>(), new StringListAttributeFactory());

    @SuppressWarnings("unchecked")
    private Map<String, BooleanAttribute> portletPreferenceReadOnly =
            LazyMap.decorate(
                    new HashMap<String, BooleanAttribute>(), new BooleanAttributeFactory());

    private final Logger logger = LoggerFactory.getLogger(getClass());

    /** Default constructor */
    public PortletDefinitionForm() {}

    /** Construct a new PortletDefinitionForm from a PortletDefinition */
    public PortletDefinitionForm(IPortletDefinition def) {
        this.setId(def.getPortletDefinitionId().getStringId());
        this.setFname(def.getFName());
        this.setName(def.getName());
        this.setDescription(def.getDescription());
        this.setTitle(def.getTitle());
        this.setTimeout(def.getTimeout());
        this.setTypeId(def.getType().getId());
        this.setApplicationId(def.getPortletDescriptorKey().getWebAppName());
        this.setPortletName(def.getPortletDescriptorKey().getPortletName());
        this.setFramework(def.getPortletDescriptorKey().isFrameworkPortlet());
        if (def.getParameter(IPortletDefinition.EDITABLE_PARAM) != null) {
            this.setEditable(
                    Boolean.parseBoolean(
                            def.getParameter(IPortletDefinition.EDITABLE_PARAM).getValue()));
        }
        if (def.getParameter(IPortletDefinition.CONFIGURABLE_PARAM) != null) {
            this.setConfigurable(
                    Boolean.parseBoolean(
                            def.getParameter(IPortletDefinition.CONFIGURABLE_PARAM).getValue()));
        }
        if (def.getParameter(IPortletDefinition.HAS_HELP_PARAM) != null) {
            this.setHasHelp(
                    Boolean.parseBoolean(
                            def.getParameter(IPortletDefinition.HAS_HELP_PARAM).getValue()));
        }
        if (def.getParameter(IPortletDefinition.HAS_ABOUT_PARAM) != null) {
            this.setHasAbout(
                    Boolean.parseBoolean(
                            def.getParameter(IPortletDefinition.HAS_ABOUT_PARAM).getValue()));
        }

        /*
         * Lifecycle
         */
        final PortletLifecycleState lifecycleState = def.getLifecycleState();
        this.setLifecycleState(lifecycleState);
        final IPortletLifecycleEntry lastLifecycleEntry =
                def.getLifecycle().isEmpty()
                        ? null
                        : def.getLifecycle().get(def.getLifecycle().size() - 1);
        if (lastLifecycleEntry != null
                && !lastLifecycleEntry.getLifecycleState().equals(lifecycleState)) {
            /*
             * We're in one state, but there's a future date
             * where we automatically switch to another.
             */
            switch (lastLifecycleEntry.getLifecycleState()) {
                case PUBLISHED:
                    this.setPublishDateTime(lastLifecycleEntry.getDate());
                    break;
                case EXPIRED:
                    this.setExpirationDateTime(lastLifecycleEntry.getDate());
                    break;
                default:
                    // Other lifecycle states are not affected by this consideration
            }
        }
        // MAINTENANCE lifecycle state may leverage additional metadata
        final IPortletDefinitionParameter messageParam =
                def.getParameter(PortletLifecycleState.CUSTOM_MAINTENANCE_MESSAGE_PARAMETER_NAME);
        logger.debug(
                "lastLifecycleEntry='{}'; messageParam='{}'", lastLifecycleEntry, messageParam);
        if (messageParam != null && StringUtils.isNotBlank(messageParam.getValue())) {
            setCustomMaintenanceMessage(messageParam.getValue());
        }

        final IPortletDefinitionParameter stopImmediatelyParam = def.getParameter("stopImmediately");
        if (stopImmediatelyParam != null) {
            setStopImmediately(StringUtils.equals(stopImmediatelyParam.getValue(), "true"));
        } else {
            setStopImmediately(true);
        }
        final IPortletDefinitionParameter stopDateParam = def.getParameter("stopDate");
        if (stopDateParam != null) {
            setStopDate(stopDateParam.getValue());
        }
        final IPortletDefinitionParameter stopTimeParam = def.getParameter("stopTime");
        if (stopTimeParam != null) {
            setStopTime(stopTimeParam.getValue());
        }
        final IPortletDefinitionParameter restartManuallyParam = def.getParameter("restartManually");
        if (restartManuallyParam != null) {
            setRestartManually(StringUtils.equals(restartManuallyParam.getValue(), "true"));
        } else {
            setRestartManually(true);
        }
        final IPortletDefinitionParameter restartDateParam = def.getParameter("restartDate");
        if (restartDateParam != null) {
            setRestartDate(restartDateParam.getValue());
        }
        final IPortletDefinitionParameter restartTimeParam = def.getParameter("restartTime");
        if (restartTimeParam != null) {
            setRestartTime(restartTimeParam.getValue());
        }

        for (IPortletDefinitionParameter param : def.getParameters()) {
            if (param.getName().startsWith("PORTLET.")) {
                this.portletPreferences.put(
                        param.getName(), new StringListAttribute(new String[] {param.getValue()}));
            } else {
                this.parameters.put(param.getName(), new Attribute(param.getValue()));
            }
        }

        for (IPortletPreference pref : def.getPortletPreferences()) {
            this.portletPreferences.put(pref.getName(), new StringListAttribute(pref.getValues()));
            this.portletPreferenceReadOnly.put(
                    pref.getName(), new BooleanAttribute(pref.isReadOnly()));
        }
    }

    /** Indicates whether this portlet has been previously published. */
    public boolean isNew() {
        return id == null || id.equals("-1");
    }

    /** Sets the Java class name and parameter defaults based on the PortletPublishingDefinition. */
    public void setChannelPublishingDefinition(PortletPublishingDefinition cpd) {

        // Set appName/portletName if a descriptor is present.  If a framework
        // portlet, the applicationId is /uPortal.
        if (cpd.getPortletDescriptor() != null) {
            final PortletDescriptor pDesc = cpd.getPortletDescriptor();
            // PortletDescriptor is a class generated from XSD.  The value of
            // isIsFramework() will commonly be null.
            final boolean isFramework =
                    pDesc.isIsFramework() != null ? pDesc.isIsFramework() : false;
            applicationId = isFramework ? FRAMEWORK_PORTLET_URL : pDesc.getWebAppName();
            portletName = pDesc.getPortletName();
        }

        // set default values for all portlet parameters
        for (Step step : cpd.getSteps()) {
            if (step.getParameters() != null) {
                for (Parameter param : step.getParameters()) {
                    // if this parameter doesn't currently have a value, check
                    // for a default in the CPD
                    Attribute attribute = parameters.get(param.getName());
                    if (attribute == null
                            || attribute.getValue() == null
                            || attribute.getValue().trim().equals("")) {

                        // use the default value if one exists
                        ParameterInputType input = param.getParameterInput().getValue();
                        if (input != null) {
                            parameters.put(param.getName(), new Attribute(input.getDefault()));
                        }
                    }
                }
            }
            if (step.getPreferences() != null) {
                for (Preference pref : step.getPreferences()) {
                    // if this parameter doesn't currently have a value, check
                    // for a default in the CPD
                    if (!portletPreferences.containsKey(pref.getName())
                            || portletPreferences.get(pref.getName()).getValue().size() == 0
                            || (portletPreferences.get(pref.getName()).getValue().size() == 1
                                    && portletPreferences
                                            .get(pref.getName())
                                            .getValue()
                                            .get(0)
                                            .trim()
                                            .equals(""))) {

                        if (!portletPreferences.containsKey(pref.getName())) {
                            portletPreferences.put(pref.getName(), new StringListAttribute());
                        }

                        // use the default value if one exists
                        PreferenceInputType input = pref.getPreferenceInput().getValue();
                        if (input instanceof SingleValuedPreferenceInputType) {
                            SingleValuedPreferenceInputType singleValued =
                                    (SingleValuedPreferenceInputType) input;
                            if (singleValued.getDefault() != null) {
                                portletPreferences
                                        .get(pref.getName())
                                        .getValue()
                                        .add(singleValued.getDefault());
                            }
                        } else if (input instanceof MultiValuedPreferenceInputType) {
                            MultiValuedPreferenceInputType multiValued =
                                    (MultiValuedPreferenceInputType) input;
                            if (multiValued.getDefaults() != null) {
                                portletPreferences
                                        .get(pref.getName())
                                        .getValue()
                                        .addAll(multiValued.getDefaults());
                            }
                        }
                    }
                }
            }
        }
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFname() {
        return fname;
    }

    public void setFname(String name) {
        fname = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPortlet() {
        return true;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }

    public String getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(String applicationId) {
        // Be careful not to pass on extra whitespace
        this.applicationId =
                StringUtils.isNotBlank(applicationId) ? applicationId.trim() : applicationId;
    }

    public String getPortletName() {
        return portletName;
    }

    public void setPortletName(String portletName) {
        // Be careful not to pass on extra whitespace
        this.portletName = portletName.trim();
    }

    public boolean isFramework() {
        return framework;
    }

    public void setFramework(boolean framework) {
        this.framework = framework;
    }

    public void setLifecycleState(PortletLifecycleState lifecycleState) {
        this.lifecycleState = lifecycleState;
    }

    public void setLifecycleState(String lifecycleState) {
        for (PortletLifecycleState state : PortletLifecycleState.values()) {
            if (state.toString().equals(lifecycleState)) {
                this.lifecycleState = state;
                break;
            }
        }
    }

    public PortletLifecycleState getLifecycleState() {
        return lifecycleState;
    }

    public Date getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }

    public boolean isConfigurable() {
        return configurable;
    }

    public void setConfigurable(boolean configurable) {
        this.configurable = configurable;
    }

    public boolean isHasHelp() {
        return hasHelp;
    }

    public void setHasHelp(boolean hasHelp) {
        this.hasHelp = hasHelp;
    }

    public boolean isHasAbout() {
        return hasAbout;
    }

    public void setHasAbout(boolean hasAbout) {
        this.hasAbout = hasAbout;
    }

    public Map<String, Attribute> getParameters() {
        return parameters;
    }

    public void setParameters(Map<String, Attribute> parameters) {
        this.parameters = parameters;
    }

    public Map<String, StringListAttribute> getPortletPreferences() {
        return this.portletPreferences;
    }

    public void setPortletPreferences(Map<String, StringListAttribute> portletParameters) {
        this.portletPreferences = portletParameters;
    }

    public Map<String, BooleanAttribute> getPortletPreferenceReadOnly() {
        return this.portletPreferenceReadOnly;
    }

    public void setPortletPreferenceReadOnly(
            Map<String, BooleanAttribute> portletPreferenceReadOnly) {
        this.portletPreferenceReadOnly = portletPreferenceReadOnly;
    }

    public SortedSet<JsonEntityBean> getPrincipals() {
        return Collections.unmodifiableSortedSet(principals);
    }

    /**
     * Replaces this form's collection of principals and <em>always</em> sets default permissions
     * (SUBSCRIBE+BROWSE) for new principals
     *
     * @param principals
     */
    public void setPrincipals(Set<JsonEntityBean> principals) {
        setPrincipals(principals, true);
    }

    /**
     * Replaces this form's collection of principals and <em>optionally</em> sets default
     * permissions (SUBSCRIBE+BROWSE) for new principals
     *
     * @param newPrincipals
     * @param initPermissionsForNew Give new principals <code>BROWSE</code> and <code>SUBSCRIBE
     *     </code> permission when true
     */
    public void setPrincipals(Set<JsonEntityBean> newPrincipals, boolean initPermissionsForNew) {

        final Set<JsonEntityBean> previousPrincipals = new HashSet<>(principals);
        principals.clear();
        principals.addAll(newPrincipals);

        if (initPermissionsForNew) {
            principals.stream()
                    .forEach(
                            bean -> {
                                if (!previousPrincipals.contains(bean)) {
                                    /*
                                     * Previously unknown principals receive BROWSE & SUBSCRIBE by
                                     * default (but not CONFIGURE!);  known principals do not receive
                                     * this treatment b/c we don't want to reset previous selections.
                                     */
                                    initPermissionsForPrincipal(bean);
                                }
                            });
        }
    }

    /**
     * Sets the default collection of permissions for newly-added principals. They are BROWSE and
     * SUBSCRIBE.
     *
     * @since 5.0
     */
    private void initPermissionsForPrincipal(JsonEntityBean principal) {
        permissions.add(
                principal.getTypeAndIdHash()
                        + "_"
                        + PortletAdministrationHelper.PortletPermissionsOnForm.BROWSE
                                .getActivity());
        permissions.add(
                principal.getTypeAndIdHash()
                        + "_"
                        + PortletAdministrationHelper.PortletPermissionsOnForm.SUBSCRIBE
                                .getActivity());
    }

    public Set<String> getPermissions() {
        return Collections.unmodifiableSet(permissions);
    }

    public void setPermissions(Set<String> permissions) {
        this.permissions.clear();
        this.permissions.addAll(permissions);
    }

    public void addPermission(String permission) {
        permissions.add(permission);
    }

    public void clearPermissions() {
        permissions.clear();
    }

    public SortedSet<JsonEntityBean> getCategories() {
        return Collections.unmodifiableSortedSet(categories);
    }

    public void setCategories(SortedSet<JsonEntityBean> categories) {
        this.categories.clear();
        this.categories.addAll(categories);
    }

    public void addCategory(JsonEntityBean category) {
        categories.add(category);
    }

    public int getPublishHour() {
        return publishHour;
    }

    public void setPublishHour(int publishHour) {
        this.publishHour = publishHour;
    }

    public int getPublishMinute() {
        return publishMinute;
    }

    public void setPublishMinute(int publishMinute) {
        this.publishMinute = publishMinute;
    }

    public int getPublishAmPm() {
        return publishAmPm;
    }

    public void setPublishAmPm(int publishAmPm) {
        this.publishAmPm = publishAmPm;
    }

    public int getExpirationHour() {
        return expirationHour;
    }

    public void setExpirationHour(int expirationHour) {
        this.expirationHour = expirationHour;
    }

    public int getExpirationMinute() {
        return expirationMinute;
    }

    public void setExpirationMinute(int expirationMinute) {
        this.expirationMinute = expirationMinute;
    }

    public int getExpirationAmPm() {
        return expirationAmPm;
    }

    public void setExpirationAmPm(int expirationAmPm) {
        this.expirationAmPm = expirationAmPm;
    }

    public String getCustomMaintenanceMessage() {
        return customMaintenanceMessage;
    }

    public void setCustomMaintenanceMessage(String customMaintenanceMessage) {
        this.customMaintenanceMessage = customMaintenanceMessage;
    }

    public boolean getStopImmediately() {
        return stopImmediately;
    }

    public void setStopImmediately(boolean stopImmediately) {
        this.stopImmediately = stopImmediately;
    }

    public String getStopDate() {
        return stopDate;
    }

    public void setStopDate(String stopDate) {
        this.stopDate = stopDate;
    }

    public String getStopTime() {
        return stopTime;
    }

    public void setStopTime(String stopTime) {
        this.stopTime = stopTime;
    }

    public boolean getRestartManually() {
        return restartManually;
    }

    public void setRestartManually(boolean restartManually) {
        this.restartManually = restartManually;
    }

    public String getRestartDate() {
        return restartDate;
    }

    public void setRestartDate(String restartDate) {
        this.restartDate = restartDate;
    }

    public String getRestartTime() {
        return restartTime;
    }

    public void setRestartTime(String restartTime) {
        this.restartTime = restartTime;
    }

    /**
     * Return the full date and time at which this portlet shoudl be automatically published. This
     * value is built from the individual date/time fields.
     *
     * @return
     */
    public Date getPublishDateTime() {
        if (getPublishDate() == null) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(getPublishDate());
        cal.set(Calendar.HOUR, getPublishHour());
        cal.set(Calendar.MINUTE, getPublishMinute());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.AM_PM, getPublishAmPm());
        return cal.getTime();
    }

    /**
     * Return the full date and time at which this portlet shoudl be automatically expired. This
     * value is built from the individual date/time fields.
     *
     * @return
     */
    public Date getExpirationDateTime() {
        if (getExpirationDate() == null) {
            return null;
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(getExpirationDate());
        cal.set(Calendar.HOUR, getExpirationHour());
        cal.set(Calendar.MINUTE, getExpirationMinute());
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        cal.set(Calendar.AM_PM, getExpirationAmPm());
        return cal.getTime();
    }

    /**
     * Get the expiration date as a string. This is just webflow workaround.
     *
     * @return the expiration date as a string
     */
    public String getExpirationDateString() {
        if (expirationDate == null) {
            return null;
        }

        return dateFormat.format(expirationDate);
    }

    /**
     * Set the expiration date as a string.
     *
     * @param date the date string
     * @throws ParseException if the string cannot be parsed
     */
    public void setExpirationDateString(String date) throws ParseException {
        if (StringUtils.isBlank(date)) {
            expirationDate = null;
            return;
        }

        expirationDate = dateFormat.parse(date);
    }

    public void setPublishDateTime(Date publish) {
        if (publish != null) {
            setPublishDate(publish);
            Calendar cal = Calendar.getInstance();
            cal.setTime(publish);
            if (cal.get(Calendar.HOUR) == 0) {
                setPublishHour(12);
            } else {
                setPublishHour(cal.get(Calendar.HOUR));
            }
            setPublishMinute(cal.get(Calendar.MINUTE));
            setPublishAmPm(cal.get(Calendar.AM_PM));
        }
    }

    /**
     * Get the publish date as a string.
     *
     * @return the publish date as a string
     */
    public String getPublishDateString() {
        if (publishDate == null) {
            return null;
        }

        return dateFormat.format(publishDate);
    }

    /**
     * Set the publish date as a string. This is just a webflow workaround.
     *
     * @param date the date string
     * @throws ParseException if the date cannot be parsed
     */
    public void setPublishDateString(String date) throws ParseException {
        if (StringUtils.isBlank(date)) {
            publishDate = null;
            return;
        }

        publishDate = dateFormat.parse(date);
    }

    public void setExpirationDateTime(Date expire) {
        if (expire != null) {
            setExpirationDate(expire);
            Calendar cal = Calendar.getInstance();
            cal.setTime(expire);
            if (cal.get(Calendar.HOUR) == 0) {
                setExpirationHour(12);
            } else {
                setExpirationHour(cal.get(Calendar.HOUR));
            }
            setExpirationMinute(cal.get(Calendar.MINUTE));
            setExpirationAmPm(cal.get(Calendar.AM_PM));
        }
    }
}
