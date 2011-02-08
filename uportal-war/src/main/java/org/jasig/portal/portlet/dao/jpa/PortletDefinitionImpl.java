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

package org.jasig.portal.portlet.dao.jpa;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletDefinitionParameter;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.om.PortletLifecycleState;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_PORTLET_DEF")
@GenericGenerator(
        name = "UP_PORTLET_DEF_GEN", 
        strategy = "native", 
        parameters = {
            @Parameter(name = "sequence", value = "UP_PORTLET_DEF_SEQ"),
            @Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
            @Parameter(name = "column", value = "NEXT_UP_PORTLET_DEF_HI")
        }
    )
class PortletDefinitionImpl implements IPortletDefinition {
    //Properties are final to stop changes in code, hibernate overrides the final via reflection to set their values
    @Id
    @GeneratedValue(generator = "UP_PORTLET_DEF_GEN")
    @Column(name = "PORTLET_DEF_ID")
    private final long internalPortletDefinitionId;
    
    //Hidden reference to the parent portlet definition, used by hibernate for referential integrety MUST BE LAZY FETCH
    @SuppressWarnings("unused")
    @OneToMany(mappedBy = "portletDefinition", targetEntity = PortletEntityImpl.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY)
    private Set<IPortletEntity> portletEntities = null;

    @OneToOne(targetEntity = PortletPreferencesImpl.class, cascade = { CascadeType.ALL }, fetch = FetchType.EAGER)
    @JoinColumn(name = "PORTLET_PREFS_ID", nullable = false)
    @Cascade( { org.hibernate.annotations.CascadeType.DELETE_ORPHAN, org.hibernate.annotations.CascadeType.ALL })
    private IPortletPreferences portletPreferences = null;
    

    @Transient
    private IPortletDefinitionId portletDefinitionId = null;

    @Column(name = "PORTLET_NAME", length = 128, nullable = false, unique = true)
    private String name;

	@Column(name = "PORTLET_FNAME", length = 255, nullable = false, unique = true)
	@Type(type = "fname")
	@Index(name = "IDX_PORTLET_DEF__FNAME")
	private String fname;
	
	@Column(name = "PORTLET_APPLICATION_ID", length = 255)
	private String applicationId;

	@Column(name = "PORTLET_APPLICATION_NAME", length = 255, nullable = false)
	private String portletName;

	@Column(name = "PORTLET_FRAMEWORK", nullable = false)
	private boolean isFramework;

    @Column(name = "PORTLET_TITLE", length = 128, nullable = false)
    private String title;

    @ManyToOne(targetEntity = PortletTypeImpl.class, optional = false)
    @JoinColumn(name = "PORTLET_TYPE_ID", nullable = false)
    private IPortletType portletType;
    
	@Column(name = "PORTLET_DESC", length = 255)
	private String description;

	@Column(name = "PORTLET_TIMEOUT", nullable = false)
	private int timeout = 20000; //Default to a reasonable value

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

	@Column(name = "PORTLET_EDITABLE", nullable = false)
	private boolean editable = false;

	@Column(name = "PORTLET_HAS_HELP", nullable = false)
	private boolean hasHelp = false;

	@Column(name = "PORTLET_HAS_ABOUT", nullable = false)
	private boolean hasAbout = false;

	@org.hibernate.annotations.CollectionOfElements(fetch = FetchType.EAGER, targetElement = PortletDefinitionParameterImpl.class)
	@JoinTable(name = "UP_PORTLET_PARAM", joinColumns = @JoinColumn(name = "PORTLET_ID"))
	private Set<IPortletDefinitionParameter> parameters = new HashSet<IPortletDefinitionParameter>();

	@org.hibernate.annotations.CollectionOfElements(fetch = FetchType.EAGER)
	@JoinTable(name = "UP_PORTLET_MDATA", joinColumns = @JoinColumn(name = "PORTLET_ID"))
	@org.hibernate.annotations.MapKey(columns = @Column(name = "LOCALE", length = 64, nullable = false))
	private Map<String, PortletLocalizationData> localizations = new HashMap<String, PortletLocalizationData>();

    /**
     * Used to initialize fields after persistence actions.
     */
    @SuppressWarnings("unused")
    @PostLoad
    @PostPersist
    @PostUpdate
    @PostRemove
    private void init() {
        this.portletDefinitionId = new PortletDefinitionIdImpl(this.internalPortletDefinitionId);
    }
    
    
    /**
     * Used by the ORM layer to create instances of the object.
     */
    @SuppressWarnings("unused")
    private PortletDefinitionImpl() {
        this.internalPortletDefinitionId = -1;
        this.portletPreferences = null;
    }
    
    public PortletDefinitionImpl(IPortletType portletType, String fname, String name, String title, String applicationId, String portletName, boolean isFramework) {
        Validate.notNull(portletType);
        Validate.notNull(name);
        Validate.notNull(fname);
        Validate.notNull(title);
        if (!isFramework) {
        	Validate.notNull(applicationId);
        }
        Validate.notNull(portletName);
        
        this.internalPortletDefinitionId = -1;
        this.portletPreferences = new PortletPreferencesImpl();
        this.portletType = portletType;
        this.name = name;
        this.fname = fname;
        this.title = title;
        this.applicationId = applicationId;
        this.portletName = portletName;
        this.isFramework = isFramework;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletDefinition#getPortletDefinitionId()
     */
    public IPortletDefinitionId getPortletDefinitionId() {
        return this.portletDefinitionId;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletDefinition#getPortletPreferences()
     */
    public IPortletPreferences getPortletPreferences() {
        return this.portletPreferences;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.IPortletDefinition#setPortletPreferences(org.jasig.portal.om.portlet.prefs.IPortletPreferences)
     */
    public void setPortletPreferences(IPortletPreferences portletPreferences) {
        Validate.notNull(portletPreferences, "portletPreferences can not be null");
        this.portletPreferences = portletPreferences;
    }
    
	public Set<IPortletEntity> getPortletEntities() {
		return portletEntities;
	}

	public void setPortletEntities(Set<IPortletEntity> portletEntities) {
		this.portletEntities = portletEntities;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getFName() {
		return fname;
	}

	public void setFName(String fname) {
		this.fname = fname;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public IPortletType getPortletType() {
		return portletType;
	}

	public void setPortletType(IPortletType portletType) {
		this.portletType = portletType;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}

	public int getPublisherId() {
		return publisherId;
	}

	public void setPublisherId(int publisherId) {
		this.publisherId = publisherId;
	}

	public int getApproverId() {
		return approverId;
	}

	public void setApproverId(int approverId) {
		this.approverId = approverId;
	}

	public int getExpirerId() {
		return expirerId;
	}

	public void setExpirerId(int expirerId) {
		this.expirerId = expirerId;
	}

	public Date getPublishDate() {
		return publishDate;
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	public Date getApprovalDate() {
		return approvalDate;
	}

	public void setApprovalDate(Date approvalDate) {
		this.approvalDate = approvalDate;
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

	public Set<IPortletDefinitionParameter> getParameters() {
		return parameters;
	}

	public void setParameters(Set<IPortletDefinitionParameter> parameters) {
		this.parameters = parameters;
	}

	public Map<String, PortletLocalizationData> getLocalizations() {
		return localizations;
	}

	public void setLocalizations(
			Map<String, PortletLocalizationData> localizations) {
		this.localizations = localizations;
	}

	public void setPortletDefinitionId(IPortletDefinitionId portletDefinitionId) {
		this.portletDefinitionId = portletDefinitionId;
	}

	/**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPortletDefinition)) {
            return false;
        }
        IPortletDefinition rhs = (IPortletDefinition) object;
        return this.getPortletDefinitionId().equals(rhs.getPortletDefinitionId());
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
        	.append(this.getPortletDefinitionId())
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("portletDefinitionId", this.portletDefinitionId)
            .toString();
    }


	@Override
	public IPortletType getType() {
		return this.portletType;
	}


	@Override
	public boolean hasHelp() {
		return this.hasHelp;
	}


	@Override
	public boolean hasAbout() {
		return this.hasAbout;
	}


	@Override
	public IPortletDefinitionParameter getParameter(String key) {
	    for (final IPortletDefinitionParameter param : this.parameters) {
	        if (param.getName().equals(key)) {
	            return param;
	        }
	    }
	    
	    return null;
	}


	public Map<String, IPortletDefinitionParameter> getParametersAsUnmodifiableMap() {
	    final Map<String, IPortletDefinitionParameter> parameterMap = new LinkedHashMap<String, IPortletDefinitionParameter>();
	    
	    for (final IPortletDefinitionParameter param : this.parameters) {
	        parameterMap.put(param.getName(), param);
	    }
	    
		return Collections.unmodifiableMap(parameterMap);
	}

	public String getName(String locale) {
		PortletLocalizationData localeData = localizations.get(locale);
		if (localeData != null && localeData.getName() != null) {
			return localeData.getName();
		}
		return name;
	}

	public String getDescription(String locale) {
		PortletLocalizationData localeData = localizations.get(locale);
		if (localeData != null && localeData.getDescription() != null) {
			return localeData.getDescription();
		}
		return description;
	}

	public String getTitle(String locale) {
		PortletLocalizationData localeData = localizations.get(locale);
		if (localeData != null && localeData.getTitle() != null) {
			return localeData.getTitle();
		}
		
		return title;
	}

	public void setType(IPortletType portletType) {
		this.portletType = portletType;
	}

	public String getApplicationId() {
		return applicationId;
	}

	public void setApplicationId(String applicationId) {
		this.applicationId = applicationId;
	}

	public String getPortletName() {
		return portletName;
	}

	public void setPortletName(String portletName) {
		this.portletName = portletName;
	}

	public boolean isFramework() {
		return isFramework;
	}

	public void setFramework(boolean isFramework) {
		this.isFramework = isFramework;
	}

	public void clearParameters() {
		parameters.clear();
	}

	public void addLocalizedDescription(String locale, String chanDesc) {
		PortletLocalizationData localeData = localizations.get(locale);
		if (localeData == null) {
			localeData = new PortletLocalizationData();
		}
		localeData.setDescription(chanDesc);
		localizations.put(locale, localeData);
	}

	public void addLocalizedName(String locale, String chanName) {
		PortletLocalizationData localeData = localizations.get(locale);
		if (localeData == null) {
			localeData = new PortletLocalizationData();
		}
		localeData.setName(chanName);
		localizations.put(locale, localeData);
	}

	public void addLocalizedTitle(String locale, String chanTitle) {
		PortletLocalizationData localeData = localizations.get(locale);
		if (localeData == null) {
			localeData = new PortletLocalizationData();
		}
		localeData.setTitle(chanTitle);
		localizations.put(locale, localeData);
	}

	public void removeParameter(IPortletDefinitionParameter parameter) {
		this.parameters.remove(parameter.getName());
	}

	public void removeParameter(String name) {
		this.parameters.remove(name);
	}

	public void replaceParameters(Set<IPortletDefinitionParameter> parameters) {
		this.parameters.clear();
		for (IPortletDefinitionParameter param : parameters) {
			this.parameters.add(new PortletDefinitionParameterImpl(param));
		}
	}
	
	public void setPortletPreferences(List<IPortletPreference> portletPreferences) {
		this.portletPreferences.setPortletPreferences(portletPreferences);
	}

	public EntityIdentifier getEntityIdentifier() {
		return new EntityIdentifier(String.valueOf(this.portletDefinitionId.getStringId()),
				IPortletDefinition.class);
	}

    public void addParameter(IPortletDefinitionParameter parameter) {
		addParameter(parameter);
	}

	public void addParameter(String name, String value) {
		addParameter(new PortletDefinitionParameterImpl(name, value));
	}

	public void addParameter(PortletDefinitionParameterImpl newParameter) {
	    final String newName = newParameter.getName();
        
	    for (final Iterator<IPortletDefinitionParameter> paramIter = this.parameters.iterator(); paramIter.hasNext();) {
	        final IPortletDefinitionParameter param = paramIter.next();
	        if (newName.equals(param.getName())) {
	            paramIter.remove();
	            break;
	        }
	    }
	    
	    this.parameters.add(newParameter);
	}

    public PortletLifecycleState getLifecycleState() {
		Date now = new Date();
		if (this.getExpirationDate() != null && this.getExpirationDate().before(now)) {
			return PortletLifecycleState.EXPIRED;
		} else if (this.getPublishDate() != null && this.getPublishDate().before(now)) {
			return PortletLifecycleState.PUBLISHED;
		} else if (this.getApprovalDate() != null && this.getApprovalDate().before(now)) {
			return PortletLifecycleState.APPROVED;
		} else {
			return PortletLifecycleState.CREATED;
		}
    }

}
