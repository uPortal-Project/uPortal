package org.jasig.portal.channel.dao.jpa;

import java.io.Serializable;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelParameter;
import org.jasig.portal.channels.error.CError;
import org.jasig.portal.channels.portlet.IPortletAdaptor;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * JPA/Hibernate implementation of the IChannelDefinition interface.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @Revision $Revision$
 */
@Entity
@Table(name = "UP_CHANNEL")
@GenericGenerator(name = "UP_CHANNEL_DEF_GEN", strategy = "native", parameters = {
		@Parameter(name = "sequence", value = "UP_CHANNEL_DEF_SEQ"),
		@Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
		@Parameter(name = "column", value = "NEXT_UP_CHANNEL_DEF_HI") })
public class ChannelDefinitionImpl implements IChannelDefinition, IBasicEntity,
		Serializable {

	@Id
	@GeneratedValue(generator = "UP_CHANNEL_DEF_GEN")
	@Column(name = "CHAN_ID")
	private Long internalId;

	@Column(name = "CHAN_FNAME", length = 255, nullable = false, unique = true)
	private String fname;

	@Column(name = "CHAN_NAME", length = 128)
	private String name;

	@Column(name = "CHAN_DESC", length = 255)
	private String description;

	@Column(name = "CHAN_TITLE", length = 128, nullable = false)
	private String title;

	@Column(name = "CHAN_CLASS", length = 100, nullable = false)
	private String clazz;

	@Column(name = "CHAN_TIMEOUT", nullable = false)
	private int timeout;

	// TODO: integrate with channel type persistence code
	@Column(name = "CHAN_TYPE_ID", nullable = false)
	private int typeId;

	@Column(name = "CHAN_PUBL_ID")
	private int publisherId;

	@Column(name = "CHAN_APVL_ID")
	private int approverId;

	@Column(name = "CHAN_EXP_ID")
	private int expirerId;

	@Column(name = "CHAN_PUBL_DT")
	private Date publishDate;

	@Column(name = "CHAN_APVL_DT")
	private Date approvalDate;

	@Column(name = "CHAN_EXP_DT")
	private Date expirationDate;

	@Column(name = "CHAN_EDITABLE", nullable = false)
	private boolean editable;

	@Column(name = "CHAN_HAS_HELP", nullable = false)
	private boolean hasHelp;

	@Column(name = "CHAN_HAS_ABOUT", nullable = false)
	private boolean hasAbout;

	@Column(name = "CHAN_SECURE", nullable = false)
	private boolean secure;

	@org.hibernate.annotations.CollectionOfElements(fetch = FetchType.EAGER)
	@JoinTable(name = "UP_CHANNEL_PARAM", joinColumns = @JoinColumn(name = "CHAN_ID"))
	private Set<ChannelParameterImpl> parameters = new HashSet<ChannelParameterImpl>();

	@org.hibernate.annotations.CollectionOfElements(fetch = FetchType.EAGER)
	@JoinTable(name = "UP_CHANNEL_MDATA", joinColumns = @JoinColumn(name = "CHAN_ID"))
	@org.hibernate.annotations.MapKey(columns = @Column(name = "LOCALE", length = 64))
	private Map<String, ChannelLocalizationData> localizations = new HashMap<String, ChannelLocalizationData>();

	// TODO: integrate with portlet persistence code
	@Transient
	private Map<String, IPortletPreference> portletPreferences = new HashMap<String, IPortletPreference>();

	@Transient
	private String locale; // this probably shouldn't be a channel property?

	/**
	 * Default constructor
	 */
	public ChannelDefinitionImpl() {
	}

	/**
	 * Construct a new ChannelDefinitionImpl with the specified ID.
	 * 
	 * @param id
	 */
	public ChannelDefinitionImpl(int id) {
		if (id >= 0) {
			this.internalId = new Long(id);
		}
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

	public void addParameter(ChannelParameterImpl parameter) {
		for (Iterator<ChannelParameterImpl> iter = this.parameters.iterator(); iter
				.hasNext();) {
			ChannelParameterImpl param = iter.next();
			if (param.getName().equals(parameter.getName())) {
				param = parameter;
				return;
			}
		}

		this.parameters.add(parameter);
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
		} else {
			return description;
		}
	}

	public EntityIdentifier getEntityIdentifier() {
		return new EntityIdentifier(String.valueOf(this.getId()),
				ChannelDefinitionImpl.class);
	}

	public String getFName() {
		return fname;
	}

	public int getId() {
		if (internalId == null) {
			return -1;
		} else {
			return internalId.intValue();
		}
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
		} else {
			return name;
		}
	}

	public IChannelParameter getParameter(String key) {
		for (IChannelParameter param : this.parameters) {
			if (param.getName().equals(key)) {
				return param;
			}
		}
		return null;
	}

	public Set<IChannelParameter> getParameters() {
		Set<IChannelParameter> params = new HashSet<IChannelParameter>();
		for (IChannelParameter param : this.parameters) {
			params.add(param);
		}
		return params;
	}

	public Map<String, IChannelParameter> getParametersAsUnmodifiableMap() {
		Map<String, IChannelParameter> map = new HashMap<String, IChannelParameter>();
		for (IChannelParameter param : this.parameters) {
			map.put(param.getName(), param);
		}
		return Collections.unmodifiableMap(map);
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
		} else {
			return title;
		}
	}

	public int getTypeId() {
		return typeId;
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
		if (!StringUtils.isBlank(this.clazz)) {
			try {
				final Class<?> channelClazz = Class.forName(this.clazz);
				return IPortletAdaptor.class.isAssignableFrom(channelClazz);
			} catch (ClassNotFoundException e) {
			}
		}
		return false;
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

	public void replacePortletPreference(
			List<IPortletPreference> portletPreferences) {
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

	public void setFName(String fname) {
		this.fname = fname;
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

	public void setJavaClass(String javaClass) {
		this.clazz = javaClass;
	}

	public void setLocale(String locale) {
		this.locale = locale;
	}

	public void setName(String name) {
		this.name = name;
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

	public void setTitle(String title) {
		this.title = title;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}
	
	
	// Legacy methods
	// TODO: these helper methods should all be transferred elsewhere

	/**
	 * Get an Element expressing the minimum attributes necessary to represent a
	 * channel.
	 * 
	 * @param doc
	 *            Document that will be the owner of the Element returned
	 * @param idTag
	 *            Value of the identifier for the channel
	 * @param chanClassArg
	 *            fully qualified class name of the channel
	 * @param editable
	 *            true if the channel handles the Edit event
	 * @param hasHelp
	 *            true if the channel handles the Help event
	 * @param hasAbout
	 *            true if the channel handles the About event
	 * @return Element representing the channel
	 */
	private Element getBase(Document doc, String idTag, String chanClassArg,
			boolean editable, boolean hasHelp, boolean hasAbout) {
		Element channel = doc.createElement("channel");

		// the ID attribute is the identifier for the Channel element
		channel.setAttribute("ID", idTag);
		channel.setIdAttribute("ID", true);

		channel.setAttribute("chanID", this.internalId + "");
		channel.setAttribute("timeout", this.timeout + "");
		if (this.locale != null) {
			channel.setAttribute("name", getName(this.locale));
			channel.setAttribute("title", getTitle(this.locale));
			channel.setAttribute("locale", this.locale);
		} else {
			channel.setAttribute("name", this.name);
			channel.setAttribute("title", this.title);
		}
		channel.setAttribute("fname", this.fname);

		// chanClassArg is so named to highlight that we are using the argument
		// to the method rather than the instance variable chanClass
		channel.setAttribute("class", chanClassArg);
		channel.setAttribute("typeID", this.typeId + "");
		channel.setAttribute("editable", editable ? "true" : "false");
		channel.setAttribute("hasHelp", hasHelp ? "true" : "false");
		channel.setAttribute("hasAbout", hasAbout ? "true" : "false");
		channel.setAttribute("secure", this.secure ? "true" : "false");
		channel.setAttribute("isPortlet", Boolean.valueOf(this.isPortlet())
				.toString());

		return channel;
	}

	private final Element nodeParameter(Document doc, String name, int value) {
		return nodeParameter(doc, name, Integer.toString(value));
	}

	private final Element nodeParameter(Document doc, String name, String value) {
		Element parameter = doc.createElement("parameter");
		parameter.setAttribute("name", name);
		parameter.setAttribute("value", value);
		return parameter;
	}

	private final void addParameters(Document doc, Element channel) {
		if (parameters != null) {
			Iterator<ChannelParameterImpl> iter = parameters.iterator();
			while (iter.hasNext()) {
				IChannelParameter cp = iter.next();

				Element parameter = nodeParameter(doc, cp.getName(), cp
						.getValue());
				if (cp.getOverride()) {
					parameter.setAttribute("override", "yes");
				} else {
					parameter.setAttribute("override", "no");
				}
				channel.appendChild(parameter);
			}
		}
	}

	/**
	 * Display a message where this channel should be
	 */
	@Deprecated
	public Element getDocument(Document doc, String idTag, String statusMsg,
			int errorId) {
		Element channel = getBase(doc, idTag, CError.class.getName(), false,
				false, false);
		addParameters(doc, channel);
		channel.appendChild(nodeParameter(doc, "CErrorMessage", statusMsg));
		channel.appendChild(nodeParameter(doc, "CErrorChanId", idTag));
		channel.appendChild(nodeParameter(doc, "CErrorErrorId", errorId));
		return channel;
	}

	/**
	 * return an xml representation of this channel
	 */
	@Deprecated
	public Element getDocument(Document doc, String idTag) {
		Element channel = getBase(doc, idTag, this.clazz, this.editable,
				this.hasHelp, this.hasAbout);
		channel.setAttribute("description", this.description);
		addParameters(doc, channel);
		return channel;
	}

}
