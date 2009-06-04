package org.jasig.portal.channel.dao.jpa;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.PostLoad;
import javax.persistence.PostPersist;
import javax.persistence.PostRemove;
import javax.persistence.PostUpdate;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.channel.IChannelType;
import org.jasig.portal.channel.XmlGeneratingBaseChannelDefinition;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.portlet.om.IPortletPreference;

/**
 * JPA/Hibernate implementation of the IChannelDefinition interface.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @Revision $Revision$
 */
@SuppressWarnings("deprecation")
@Entity
@Table(name = "UP_CHANNEL")
@GenericGenerator(name = "UP_CHANNEL_DEF_GEN", strategy = "native", parameters = {
		@Parameter(name = "sequence", value = "UP_CHANNEL_DEF_SEQ"),
		@Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
		@Parameter(name = "column", value = "NEXT_UP_CHANNEL_DEF_HI") })
class ChannelDefinitionImpl extends XmlGeneratingBaseChannelDefinition implements IChannelDefinition, IBasicEntity,
		Serializable {
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(generator = "UP_CHANNEL_DEF_GEN")
	@Column(name = "CHAN_ID")
	private final int internalId;

    @Column(name = "CHAN_NAME", length = 128, nullable = false, unique = true)
    private String name;

	@Column(name = "CHAN_FNAME", length = 255, nullable = false, unique = true)
	@Type(type = "fname")
	@Index(name = "IDX_CHAN_DEF__FNAME")
	private String fname;

    @Column(name = "CHAN_TITLE", length = 128, nullable = false)
    private String title;

    @Column(name = "CHAN_CLASS", length = 100, nullable = false)
    private String clazz;

    @ManyToOne(targetEntity = ChannelTypeImpl.class, optional = false)
    @JoinColumn(name = "CHAN_TYPE_ID", nullable = false)
    private IChannelType channelType;
    
	@Column(name = "CHAN_DESC", length = 255)
	private String description;

	@Column(name = "CHAN_TIMEOUT", nullable = false)
	private int timeout = 20000; //Default to a reasonable value

	//TODO link to User object once it is JPA managed
	@Column(name = "CHAN_PUBL_ID")
	private int publisherId = -1;

	//TODO link to User object once it is JPA managed
	@Column(name = "CHAN_APVL_ID")
	private int approverId = -1;

	//TODO link to User object once it is JPA managed
	@Column(name = "CHAN_EXP_ID")
	private int expirerId = -1;

	@Column(name = "CHAN_PUBL_DT")
	private Date publishDate = null;

	@Column(name = "CHAN_APVL_DT")
	private Date approvalDate = null;

	@Column(name = "CHAN_EXP_DT")
	private Date expirationDate = null;

	@Column(name = "CHAN_EDITABLE", nullable = false)
	private boolean editable = false;

	@Column(name = "CHAN_HAS_HELP", nullable = false)
	private boolean hasHelp = false;

	@Column(name = "CHAN_HAS_ABOUT", nullable = false)
	private boolean hasAbout = false;

	@Column(name = "CHAN_SECURE", nullable = false)
	private boolean secure = false;

	@org.hibernate.annotations.CollectionOfElements(fetch = FetchType.EAGER, targetElement = ChannelParameterImpl.class)
	@JoinTable(name = "UP_CHANNEL_PARAM", joinColumns = @JoinColumn(name = "CHAN_ID"))
	private Set<IChannelParameter> parameters = new HashSet<IChannelParameter>();

	@org.hibernate.annotations.CollectionOfElements(fetch = FetchType.EAGER)
	@JoinTable(name = "UP_CHANNEL_MDATA", joinColumns = @JoinColumn(name = "CHAN_ID"))
	@org.hibernate.annotations.MapKey(columns = @Column(name = "LOCALE", length = 64))
	private Map<String, ChannelLocalizationData> localizations = new HashMap<String, ChannelLocalizationData>();

	// TODO: integrate with portlet persistence code
	@Transient
	private Map<String, IPortletPreference> portletPreferences = new HashMap<String, IPortletPreference>();

	@Transient
	private String locale; // this probably shouldn't be a channel property?
	
	@Transient
	private boolean isPortlet = false;
	
	
    /**
     * Used to initialize fields after persistence actions.
     */
    @PostLoad
    @PostPersist
    @PostUpdate
    @PostRemove
    private void initClass() {
        if (!StringUtils.isBlank(this.clazz)) {
            try {
                final Class<?> channelClazz = Class.forName(this.clazz);
                this.isPortlet = IPortletAdaptor.class.isAssignableFrom(channelClazz);
                return;
            }
            catch (ClassNotFoundException e) {
            }
        }
        
        this.isPortlet = false;
    }

	/*
	 * Internal, for hibernate
	 */
	@SuppressWarnings("unused")
    private ChannelDefinitionImpl() {
        this.internalId = -1;
        this.channelType = null;
        this.name = null;
        this.fname = null;
        this.title = null;
        this.clazz = null;
	}
	
    ChannelDefinitionImpl(IChannelType channelType, String fname, String clazz, String name, String title) {
        Validate.notNull(channelType);
        Validate.notNull(name);
        Validate.notNull(fname);
        Validate.notNull(title);
        Validate.notNull(clazz);
        
        this.internalId = -1;
        this.channelType = channelType;
        this.name = name;
        this.fname = fname;
        this.title = title;
        this.clazz = clazz;
        
        this.initClass();
    }
    
    
    public void setFName(String fname) {
        Validate.notNull(fname);
        this.fname = fname;
    }

    public void setJavaClass(String javaClass) {
        Validate.notNull(javaClass);
        this.clazz = javaClass;
        initClass();
    }

    public void setName(String name) {
        Validate.notNull(name);
        this.name = name;
    }

    public void setTitle(String title) {
        Validate.notNull(title);
        this.title = title;
    }

    public void setType(IChannelType channelType) {
        Validate.notNull(channelType);
        this.channelType = channelType;
    }

    public void addParameter(IChannelParameter parameter) {
		addParameter(parameter);
	}

	public void addParameter(String name, String value, String override) {
		addParameter(new ChannelParameterImpl(name, value, Boolean.valueOf(
				override).booleanValue()));
	}

	public void addParameter(String name, String value, boolean override) {
		addParameter(new ChannelParameterImpl(name, value, override));
	}

	public void addParameter(ChannelParameterImpl newParameter) {
	    final String newName = newParameter.getName();
        
	    for (final Iterator<IChannelParameter> paramIter = this.parameters.iterator(); paramIter.hasNext();) {
	        final IChannelParameter param = paramIter.next();
	        if (newName.equals(param.getName())) {
	            paramIter.remove();
	            break;
	        }
	    }
	    
	    this.parameters.add(newParameter);
	}

	public void clearParameters() {
		parameters.clear();
	}

	public Date getApprovalDate() {
		return approvalDate;
	}

	public int getApproverId() {
		return approverId;
	}

	public String getDescription() {
		return description;
	}

	public String getDescription(String locale) {
		ChannelLocalizationData localeData = localizations.get(locale);
		if (localeData != null && localeData.getDescription() != null) {
			return localeData.getDescription();
		}
		return description;
	}

	public EntityIdentifier getEntityIdentifier() {
		return new EntityIdentifier(String.valueOf(this.getId()),
				ChannelDefinitionImpl.class);
	}

	public String getFName() {
		return fname;
	}

	public int getId() {
	    return this.internalId;
	}

	public String getJavaClass() {
		return clazz;
	}

	public String getLocale() {
		return locale;
	}

	public String getName() {
		return name;
	}

	public String getName(String locale) {
		ChannelLocalizationData localeData = localizations.get(locale);
		if (localeData != null && localeData.getName() != null) {
			return localeData.getName();
		}
		return name;
	}

	public IChannelParameter getParameter(String key) {
	    for (final IChannelParameter param : this.parameters) {
	        if (param.getName().equals(key)) {
	            return param;
	        }
	    }
	    
	    return null;
	}

	public Set<IChannelParameter> getParameters() {
	    return new LinkedHashSet<IChannelParameter>(this.parameters);
	}

	public Map<String, IChannelParameter> getParametersAsUnmodifiableMap() {
	    final Map<String, IChannelParameter> parameterMap = new LinkedHashMap<String, IChannelParameter>();
	    
	    for (final IChannelParameter param : this.parameters) {
	        parameterMap.put(param.getName(), param);
	    }
	    
		return Collections.unmodifiableMap(parameterMap);
	}

	public IPortletPreference[] getPortletPreferences() {
		return this.portletPreferences.values().toArray(
				new IPortletPreference[this.portletPreferences.size()]);
	}

	public Date getPublishDate() {
		return publishDate;
	}

	public int getPublisherId() {
		return publisherId;
	}

	public int getTimeout() {
		return timeout;
	}

	public String getTitle() {
		return title;
	}

	public String getTitle(String locale) {
		ChannelLocalizationData localeData = localizations.get(locale);
		if (localeData != null && localeData.getTitle() != null) {
			return localeData.getTitle();
		}
		
		return title;
	}

	@Deprecated
	public int getTypeId() {
		return this.channelType.getId();
	}
	
    public IChannelType getType() {
        return this.channelType;
    }

    public boolean hasAbout() {
		return hasAbout;
	}

	public boolean hasHelp() {
		return hasHelp;
	}

	public boolean isEditable() {
		return editable;
	}

	public boolean isPortlet() {
		return isPortlet;
	}

	public boolean isSecure() {
		return secure;
	}

	public void addLocalizedDescription(String locale, String chanDesc) {
		ChannelLocalizationData localeData = localizations.get(locale);
		if (localeData == null) {
			localeData = new ChannelLocalizationData();
		}
		localeData.setDescription(chanDesc);
		localizations.put(locale, localeData);
	}

	public void addLocalizedName(String locale, String chanName) {
		ChannelLocalizationData localeData = localizations.get(locale);
		if (localeData == null) {
			localeData = new ChannelLocalizationData();
		}
		localeData.setName(chanName);
		localizations.put(locale, localeData);
	}

	public void addLocalizedTitle(String locale, String chanTitle) {
		ChannelLocalizationData localeData = localizations.get(locale);
		if (localeData == null) {
			localeData = new ChannelLocalizationData();
		}
		localeData.setTitle(chanTitle);
		localizations.put(locale, localeData);
	}

	public void removeParameter(IChannelParameter parameter) {
		this.parameters.remove(parameter.getName());
	}

	public void removeParameter(String name) {
		this.parameters.remove(name);
	}

	public void replaceParameters(Set<IChannelParameter> parameters) {
		this.parameters.clear();
		for (IChannelParameter param : parameters) {
			this.parameters.add(new ChannelParameterImpl(param));
		}
	}

	public void replacePortletPreference(List<IPortletPreference> portletPreferences) {
		this.portletPreferences.clear();
		for (IPortletPreference pref : portletPreferences) {
			this.portletPreferences.put(pref.getName(), pref);
		}
	}

	public void setApprovalDate(Date approvalDate) {
		this.approvalDate = approvalDate;
	}

	public void setApproverId(int approvalId) {
		this.approverId = approvalId;
	}

	public void setDescription(String descr) {
		this.description = descr;
	}

	public void setEditable(boolean editable) {
		this.editable = editable;
	}

	public void setHasAbout(boolean hasAbout) {
		this.hasAbout = hasAbout;
	}

	public void setHasHelp(boolean hasHelp) {
		this.hasHelp = hasHelp;
	}

	public void setIsSecure(boolean isSecure) {
		this.secure = isSecure;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public void setParameters(Set<IChannelParameter> parameters) {
		for (IChannelParameter param : parameters) {
			this.parameters.add(new ChannelParameterImpl(param));
		}
	}

	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}

	public void setPublisherId(int publisherId) {
		this.publisherId = publisherId;
	}

	public void setTimeout(int timeout) {
		this.timeout = timeout;
	}
    
	
    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IChannelDefinition)) {
            return false;
        }
        IChannelDefinition rhs = (IChannelDefinition) object;
        return new EqualsBuilder()
            .append(this.fname, rhs.getFName())
            .append(this.name, rhs.getName())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143)
            .append(this.fname)
            .append(this.name)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("id", this.internalId)
            .append("publisherId", this.publisherId)
            .append("expirationDate", this.expirationDate)
            .append("approverId", this.approverId)
            .append("expirerId", this.expirerId)
            .append("locale", this.locale)
            .append("secure", this.secure)
            .append("hasHelp", this.hasHelp)
            .append("localizations", this.localizations)
            .append("hasAbout", this.hasAbout)
            .append("editable", this.editable)
            .append("title", this.title)
            .append("description", this.description)
            .append("name", this.name)
            .append("approvalDate", this.approvalDate)
            .append("parameters", this.parameters)
            .append("portletPreferences", this.portletPreferences)
            .append("clazz", this.clazz)
            .append("type", this.channelType)
            .append("isPortlet", this.isPortlet)
            .append("fname", this.fname)
            .append("timeout", this.timeout)
            .append("publishDate", this.publishDate)
            .toString();
    }

    
}
