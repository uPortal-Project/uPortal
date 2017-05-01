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
package org.apereo.portal.portlet.dao.jpa;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Transient;
import javax.persistence.Version;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.portlet.marketplace.IMarketplaceRating;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletDefinitionParameter;
import org.apereo.portal.portlet.om.IPortletDescriptorKey;
import org.apereo.portal.portlet.om.IPortletEntity;
import org.apereo.portal.portlet.om.IPortletPreference;
import org.apereo.portal.portlet.om.IPortletType;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;
import org.springframework.util.StringUtils;

/**
 */
@Entity
@Table(name = "UP_PORTLET_DEF")
@SequenceGenerator(
    name = "UP_PORTLET_DEF_GEN",
    sequenceName = "UP_PORTLET_DEF_SEQ",
    allocationSize = 5
)
@TableGenerator(name = "UP_PORTLET_DEF_GEN", pkColumnValue = "UP_PORTLET_DEF", allocationSize = 5)
@NaturalIdCache(region = "org.apereo.portal.portlet.dao.jpa.PortletDefinitionImpl-NaturalId")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PortletDefinitionImpl implements IPortletDefinition {
    //Properties are final to stop changes in code, hibernate overrides the final via reflection to set their values
    @Id
    @GeneratedValue(generator = "UP_PORTLET_DEF_GEN")
    @Column(name = "PORTLET_DEF_ID")
    private final long internalPortletDefinitionId;

    @Transient private IPortletDefinitionId portletDefinitionId = null;

    @SuppressWarnings("unused")
    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;

    //Hidden reference to the child portlet entities, used to allow cascading deletes where when a portlet definition is deleted all associated entities are also deleted
    //MUST BE LAZY FETCH, this set should never actually be populated at runtime or performance will be TERRIBLE
    @SuppressWarnings("unused")
    @OneToMany(
        mappedBy = "portletDefinition",
        targetEntity = PortletEntityImpl.class,
        cascade = {CascadeType.ALL},
        fetch = FetchType.LAZY,
        orphanRemoval = true
    )
    private Set<IPortletEntity> portletEntities = null;

    @OneToOne(
        targetEntity = PortletPreferencesImpl.class,
        cascade = {CascadeType.ALL},
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    @JoinColumn(name = "PORTLET_PREFS_ID", nullable = false)
    @Fetch(FetchMode.JOIN)
    private final PortletPreferencesImpl portletPreferences;

    /**
     * Name is used for admin tools, but will typically not be presented to end users. It allows for
     * situations where you need to define multiple similar portlets, all sharing a title. but still
     * provides a way to distinguish between the portlets in admin tools.
     */
    @Column(name = "PORTLET_NAME", length = 128, nullable = false, unique = true)
    private String name;

    @NaturalId(mutable = true)
    @Column(name = "PORTLET_FNAME", length = 255, nullable = false)
    @Type(type = "fname")
    private String fname;

    @Column(name = "PORTLET_TITLE", length = 128, nullable = false)
    @Index(name = "IDX_PORTLET_DEF__TITLE")
    private String title;

    @ManyToOne(targetEntity = PortletTypeImpl.class, optional = false)
    @JoinColumn(name = "PORTLET_TYPE_ID", nullable = false)
    private IPortletType portletType;

    @Column(name = "PORTLET_DESC", length = 255)
    private String description;

    @Column(name = "AVG_RATING")
    private Double rating;

    @Column(name = "AVG_RATING_USER_COUNT")
    private Long usersRated;

    @Column(name = "PORTLET_TIMEOUT", nullable = false)
    private int timeout = 20000; //Default to a reasonable value

    @Column(name = "ACTION_TIMEOUT")
    private Integer actionTimeout = null;

    @Column(name = "EVENT_TIMEOUT")
    private Integer eventTimeout = null;

    @Column(name = "RENDER_TIMEOUT")
    private Integer renderTimeout = null;

    @Column(name = "RESOURCE_TIMEOUT")
    private Integer resourceTimeout = null;

    //TODO link to User object once it is JPA managed
    @Column(name = "PORTLET_PUBL_ID")
    private int publisherId = -1;

    //TODO link to User object once it is JPA managed
    @Column(name = "PORTLET_APVL_ID")
    private int approverId = -1;

    //TODO link to User object once it is JPA managed
    @Column(name = "PORTLET_EXP_ID")
    private int expirerId = -1;

    @Column(name = "PORTLET_PUBL_DT")
    private Date publishDate = null;

    @Column(name = "PORTLET_APVL_DT")
    private Date approvalDate = null;

    @Column(name = "PORTLET_EXP_DT")
    private Date expirationDate = null;

    @OneToMany(
        targetEntity = PortletDefinitionParameterImpl.class,
        cascade = CascadeType.ALL,
        fetch = FetchType.EAGER,
        orphanRemoval = true
    )
    @JoinColumn(name = "PORLTET_DEF_ID", nullable = false)
    @MapKey(name = "name")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private Map<String, IPortletDefinitionParameter> parameters =
            new LinkedHashMap<String, IPortletDefinitionParameter>();

    @ElementCollection(fetch = FetchType.EAGER, targetClass = PortletLocalizationData.class)
    @JoinTable(name = "UP_PORTLET_DEF_MDATA", joinColumns = @JoinColumn(name = "PORTLET_ID"))
    @MapKeyColumn(name = "LOCALE", length = 64, nullable = false)
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private final Map<String, PortletLocalizationData> localizations =
            new LinkedHashMap<String, PortletLocalizationData>();

    @Embedded private PortletDescriptorKeyImpl portletDescriptorKey;

    /** Used to initialize fields after persistence actions. */
    @PostLoad
    @PostPersist
    @PostUpdate
    @PostRemove
    private void init() {
        if (this.internalPortletDefinitionId != -1
                && (this.portletDefinitionId == null
                        || this.portletDefinitionId.getLongId()
                                != this.internalPortletDefinitionId)) {
            this.portletDefinitionId =
                    PortletDefinitionIdImpl.create(this.internalPortletDefinitionId);
        }
    }

    /** Used by the ORM layer to create instances of the object. */
    @SuppressWarnings("unused")
    private PortletDefinitionImpl() {
        this.internalPortletDefinitionId = -1;
        this.entityVersion = -1;
        this.portletPreferences = null;
    }

    public PortletDefinitionImpl(
            IPortletType portletType,
            String fname,
            String name,
            String title,
            String applicationId,
            String portletName,
            boolean isFramework) {
        Validate.notNull(portletType);
        Validate.notNull(name);
        Validate.notNull(fname);
        Validate.notNull(title);
        if (!isFramework) {
            Validate.notNull(applicationId);
        }
        Validate.notNull(portletName);

        this.internalPortletDefinitionId = -1;
        this.entityVersion = -1;
        this.portletPreferences = new PortletPreferencesImpl();
        this.portletType = portletType;
        this.name = name;
        this.fname = fname;
        this.title = title;

        this.portletDescriptorKey = new PortletDescriptorKeyImpl();
        this.portletDescriptorKey.setWebAppName(applicationId);
        this.portletDescriptorKey.setPortletName(portletName);
        this.portletDescriptorKey.setFrameworkPortlet(isFramework);
    }

    public PortletDefinitionImpl(
            IPortletType portletType,
            String fname,
            String name,
            String title,
            String applicationId,
            String portletName,
            boolean isFramework,
            IPortletDefinitionId Id) {
        Validate.notNull(portletType);
        Validate.notNull(name);
        Validate.notNull(fname);
        Validate.notNull(title);
        if (!isFramework) {
            Validate.notNull(applicationId);
        }
        Validate.notNull(portletName);

        this.internalPortletDefinitionId = Id.getLongId();
        this.entityVersion = -1;
        this.portletPreferences = new PortletPreferencesImpl();
        this.portletType = portletType;
        this.name = name;
        this.fname = fname;
        this.title = title;

        this.portletDescriptorKey = new PortletDescriptorKeyImpl();
        this.portletDescriptorKey.setWebAppName(applicationId);
        this.portletDescriptorKey.setPortletName(portletName);
        this.portletDescriptorKey.setFrameworkPortlet(isFramework);
    }

    //** APIs for import/export support **//
    @Override
    public String getDataId() {
        return this.getFName();
    }

    @Override
    public String getDataTitle() {
        return this.getName();
    }

    @Override
    public String getDataDescription() {
        return this.getDescription();
    }

    //** APIs for portlet definitions **//

    /** @return the rating */
    @Override
    public Double getRating() {
        return rating;
    }

    /**
     * @param rating the rating to set. Must be within marketplaceRating range (inclusive). Can not
     *     be null.
     * @throws IllegalArgumentException
     */
    @Override
    public void setRating(Double rating) {
        Validate.notNull(rating, "Rating cannot be null.  Maybe you meant 0?");
        if (rating > IMarketplaceRating.MAX_RATING || rating < IMarketplaceRating.MIN_RATING) {
            throw new IllegalArgumentException();
        }
        this.rating = rating;
    }

    /** @return the count of users that rated this portlet. Will not return null */
    @Override
    public Long getUsersRated() {
        return usersRated == null ? 0 : usersRated;
    }

    /** @param usersRated - Number of users that rated this portlet. */
    @Override
    public void setUsersRated(Long usersRated) {
        Validate.isTrue(usersRated > -1L, "Number of Users that rated shouldn't be under zero");
        this.usersRated = usersRated;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.om.portlet.IPortletDefinition#getPortletDefinitionId()
     */
    @Override
    public IPortletDefinitionId getPortletDefinitionId() {
        init();
        return this.portletDefinitionId;
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.om.IPortletEntity#getPortletPreferences()
     */
    @Override
    public List<IPortletPreference> getPortletPreferences() {
        return portletPreferences.getPortletPreferences();
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.portlet.om.IPortletEntity#setPortletPreferences(java.util.List)
     */
    @Override
    public boolean setPortletPreferences(List<IPortletPreference> portletPreferences) {
        return this.portletPreferences.setPortletPreferences(portletPreferences);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getFName() {
        return fname;
    }

    @Override
    public void setFName(String fname) {
        this.fname = fname;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public void setTitle(String title) {
        this.title = title;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public int getTimeout() {
        return timeout;
    }

    @Override
    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    @Override
    public Integer getActionTimeout() {
        return actionTimeout;
    }

    @Override
    public void setActionTimeout(Integer actionTimeout) {
        this.actionTimeout = actionTimeout;
    }

    @Override
    public Integer getEventTimeout() {
        return eventTimeout;
    }

    @Override
    public void setEventTimeout(Integer eventTimeout) {
        this.eventTimeout = eventTimeout;
    }

    @Override
    public Integer getRenderTimeout() {
        return renderTimeout;
    }

    @Override
    public void setRenderTimeout(Integer renderTimeout) {
        this.renderTimeout = renderTimeout;
    }

    @Override
    public Integer getResourceTimeout() {
        return resourceTimeout;
    }

    @Override
    public void setResourceTimeout(Integer resourceTimeout) {
        this.resourceTimeout = resourceTimeout;
    }

    @Override
    public int getPublisherId() {
        return publisherId;
    }

    @Override
    public void setPublisherId(int publisherId) {
        this.publisherId = publisherId;
    }

    @Override
    public int getApproverId() {
        return approverId;
    }

    @Override
    public void setApproverId(int approverId) {
        this.approverId = approverId;
    }

    @Override
    public int getExpirerId() {
        return expirerId;
    }

    @Override
    public void setExpirerId(int expirerId) {
        this.expirerId = expirerId;
    }

    @Override
    public Date getPublishDate() {
        return publishDate;
    }

    @Override
    public void setPublishDate(Date publishDate) {
        this.publishDate = publishDate;
    }

    @Override
    public Date getApprovalDate() {
        return approvalDate;
    }

    @Override
    public void setApprovalDate(Date approvalDate) {
        this.approvalDate = approvalDate;
    }

    @Override
    public Date getExpirationDate() {
        return expirationDate;
    }

    @Override
    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    @Override
    public IPortletType getType() {
        return this.portletType;
    }

    @Override
    public String getName(String locale) {
        PortletLocalizationData localeData = localizations.get(locale);
        if (localeData != null && localeData.getName() != null) {
            return localeData.getName();
        }
        return name;
    }

    @Override
    public String getDescription(String locale) {
        PortletLocalizationData localeData = localizations.get(locale);
        if (localeData != null && localeData.getDescription() != null) {
            return localeData.getDescription();
        }
        return description;
    }

    @Override
    public String getTitle(String locale) {
        PortletLocalizationData localeData = localizations.get(locale);
        if (localeData != null && localeData.getTitle() != null) {
            return localeData.getTitle();
        }

        return title;
    }

    @Override
    public String getAlternativeMaximizedLink() {
        final IPortletDefinitionParameter alternativeMaximizedLinkParameter =
                getParameter(ALT_MAX_LINK_PARAM);

        if (null != alternativeMaximizedLinkParameter) {
            final String alternativeMaximizedLink = alternativeMaximizedLinkParameter.getValue();

            if (StringUtils.hasText(alternativeMaximizedLink)) {
                return alternativeMaximizedLink;
            }
        }

        return null;
    }

    @Override
    public String getTarget() {
        final IPortletDefinitionParameter targetParameter = getParameter(TARGET_PARAM);

        if (null != targetParameter) {
            final String target = targetParameter.getValue();

            if (StringUtils.hasText(target)) {
                return target;
            }
        }

        return null;
    }

    @Override
    public void setType(IPortletType portletType) {
        this.portletType = portletType;
    }

    @Override
    public IPortletDescriptorKey getPortletDescriptorKey() {
        return this.portletDescriptorKey;
    }

    @Override
    public void addLocalizedDescription(String locale, String chanDesc) {
        PortletLocalizationData localeData = localizations.get(locale);
        if (localeData == null) {
            localeData = new PortletLocalizationData();
        }
        localeData.setDescription(chanDesc);
        localizations.put(locale, localeData);
    }

    @Override
    public void addLocalizedName(String locale, String chanName) {
        PortletLocalizationData localeData = localizations.get(locale);
        if (localeData == null) {
            localeData = new PortletLocalizationData();
        }
        localeData.setName(chanName);
        localizations.put(locale, localeData);
    }

    @Override
    public void addLocalizedTitle(String locale, String chanTitle) {
        PortletLocalizationData localeData = localizations.get(locale);
        if (localeData == null) {
            localeData = new PortletLocalizationData();
        }
        localeData.setTitle(chanTitle);
        localizations.put(locale, localeData);
    }

    @Override
    public Set<IPortletDefinitionParameter> getParameters() {
        return Collections.unmodifiableSet(
                new LinkedHashSet<IPortletDefinitionParameter>(parameters.values()));
    }

    @Override
    public void setParameters(Set<IPortletDefinitionParameter> newParameters) {

        if (newParameters == null) {
            this.parameters = new LinkedHashMap<String, IPortletDefinitionParameter>();
        } else if (this.parameters == null) {
            this.parameters = new LinkedHashMap<String, IPortletDefinitionParameter>();
            for (final IPortletDefinitionParameter parameter : newParameters) {
                this.parameters.put(parameter.getName(), parameter);
            }
        } else {
            //Build map of existing parameters for tracking which parameters have been removed
            final Map<String, IPortletDefinitionParameter> oldPreferences =
                    new LinkedHashMap<String, IPortletDefinitionParameter>(this.parameters);

            for (final IPortletDefinitionParameter parameter : newParameters) {
                final String name = parameter.getName();

                //Remove the existing parameter from the map since it is supposed to be persisted
                final IPortletDefinitionParameter existingParameter = oldPreferences.remove(name);
                if (existingParameter == null) {
                    //New parameter, add it to the list
                    this.parameters.put(name, parameter);
                } else {
                    //Existing parameter, update the fields
                    existingParameter.setDescription(parameter.getDescription());
                    existingParameter.setValue(parameter.getValue());
                    this.parameters.put(name, existingParameter);
                }
            }

            //Remove old parameters
            this.parameters.keySet().removeAll(oldPreferences.keySet());
        }
    }

    @Override
    public IPortletDefinitionParameter getParameter(String key) {
        return this.parameters.get(key);
    }

    @Override
    public Map<String, IPortletDefinitionParameter> getParametersAsUnmodifiableMap() {
        return Collections.unmodifiableMap(this.parameters);
    }

    @Override
    public void removeParameter(IPortletDefinitionParameter parameter) {
        this.parameters.remove(parameter.getName());
    }

    @Override
    public void removeParameter(String name) {
        this.parameters.remove(name);
    }

    @Override
    public void addParameter(IPortletDefinitionParameter parameter) {
        final String name = parameter.getName();
        final IPortletDefinitionParameter existingParameter = this.parameters.get(name);
        if (existingParameter != null) {
            existingParameter.setDescription(parameter.getDescription());
            existingParameter.setValue(parameter.getValue());
        } else {
            this.parameters.put(name, parameter);
        }
    }

    @Override
    public void addParameter(String name, String value) {
        final IPortletDefinitionParameter existingParameter = this.parameters.get(name);
        if (existingParameter != null) {
            existingParameter.setValue(value);
        } else {
            this.parameters.put(name, new PortletDefinitionParameterImpl(name, value));
        }
    }

    @Override
    public EntityIdentifier getEntityIdentifier() {
        return new EntityIdentifier(
                String.valueOf(this.portletDefinitionId.getStringId()), IPortletDefinition.class);
    }

    @Override
    public PortletLifecycleState getLifecycleState() {
        final Date now = new Date();
        if (parameters.containsKey(PortletLifecycleState.MAINTENANCE_MODE_PARAMETER_NAME)) {
            return PortletLifecycleState.MAINTENANCE;
        } else if (expirationDate != null && expirationDate.before(now)) {
            return PortletLifecycleState.EXPIRED;
        } else if (publishDate != null && publishDate.before(now)) {
            return PortletLifecycleState.PUBLISHED;
        } else if (approvalDate != null && approvalDate.before(now)) {
            return PortletLifecycleState.APPROVED;
        } else {
            return PortletLifecycleState.CREATED;
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.fname == null) ? 0 : this.fname.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null) return false;
        if (!IPortletDefinition.class.isAssignableFrom(obj.getClass())) return false;
        final IPortletDefinition other = (IPortletDefinition) obj;
        if (this.fname == null) {
            if (other.getFName() != null) return false;
        } else if (!this.fname.equals(other.getFName())) return false;
        return true;
    }

    @Override
    public String toString() {

        ToStringBuilder toStringBuilder =
                new ToStringBuilder(this)
                        .append("definitionId", portletDefinitionId)
                        .append("fname", fname)
                        .append("descriptorKey", portletDescriptorKey)
                        .append("type", portletType);

        return toStringBuilder.toString();
    }
}
