package org.jasig.portal.channel;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

public interface IChannelDefinition extends IBasicEntity {

	// Getter methods
	public int getId();

	public String getFName();

	public String getName();

	public String getDescription();

	public String getTitle();

	public String getJavaClass();

	public int getTimeout();

	/**
	 * @deprecated use {@link #getType()}
	 */
	@Deprecated
	public int getTypeId();
	
	public IChannelType getType();

	public int getPublisherId();

	public int getApproverId();

	public Date getPublishDate();

	public Date getApprovalDate();
	
	public int getExpirerId();
	
	public Date getExpirationDate();

	public boolean isEditable();

	public boolean hasHelp();

	public boolean hasAbout();

	public boolean isSecure();

	/**
	 * Returns true if this channel definition defines a portlet. Returns false
	 * if this channel definition does not define a portlet or whether this
	 * channel definition defines a portlet or not cannot be determined because
	 * this definition's channel class is not set or cannot be loaded.
	 * 
	 * @return true if we know we're a portlet, false otherwise
	 */
	public boolean isPortlet();

	public Set<IChannelParameter> getParameters();

	public IChannelParameter getParameter(String key);

	public Map<String, IChannelParameter> getParametersAsUnmodifiableMap();

	public String getLocale();

	// I18n
	public String getName(String locale);

	public String getDescription(String locale);

	public String getTitle(String locale);

	// Setter methods
	public void setFName(String fname);

	public void setName(String name);

	public void setDescription(String descr);

	public void setTitle(String title);

	public void setJavaClass(String javaClass);

	public void setTimeout(int timeout);

	public void setType(IChannelType channelType);

	public void setPublisherId(int publisherId);

	public void setApproverId(int approvalId);

	public void setPublishDate(Date publishDate);

	public void setApprovalDate(Date approvalDate);

	public void setExpirerId(int expirerId);
	
	public void setExpirationDate(Date expirationDate);

	public void setEditable(boolean editable);

	public void setHasHelp(boolean hasHelp);

	public void setHasAbout(boolean hasAbout);

	public void setIsSecure(boolean isSecure);

	public void setLocale(String locale);

	public void clearParameters();

	public void setParameters(Set<IChannelParameter> parameters);

	public void replaceParameters(Set<IChannelParameter> parameters);

	public void addLocalizedTitle(String locale, String chanTitle);

	public void addLocalizedName(String locale, String chanName);

	public void addLocalizedDescription(String locale, String chanDesc);
	
	public IPortletDefinition getPortletDefinition();

	/**
	 * @deprecated Use {@link #getPortletDefinition()} and operate on the portlet preferences directly
	 */
	@Deprecated
	public IPortletPreference[] getPortletPreferences();

    /**
     * @deprecated Use {@link #getPortletDefinition()} and operate on the portlet preferences directly
     */
	@Deprecated
	public void replacePortletPreference(
			List<IPortletPreference> portletPreferences);

	/**
	 * Implementation required by IBasicEntity interface.
	 * 
	 * @return EntityIdentifier
	 */
	public EntityIdentifier getEntityIdentifier();

	/**
	 * Adds a parameter to this channel definition
	 * 
	 * @param parameter
	 *            the channel parameter to add
	 */
	public void addParameter(IChannelParameter parameter);

	/**
	 * @deprecated Use {@link #addParameter(String, String, boolean)} instead.
	 */
	public void addParameter(String name, String value, String override);

	/**
	 * Adds a parameter to this channel definition
	 * 
	 * @param name
	 *            the channel parameter name
	 * @param value
	 *            the channel parameter value
	 * @param override
	 *            the channel parameter override setting
	 * 
	 */
	public void addParameter(String name, String value, boolean override);

	/**
	 * Removes a parameter from this channel definition
	 * 
	 * @param parameter
	 *            the channel parameter to remove
	 */
	public void removeParameter(IChannelParameter parameter);

	/**
	 * Removes a parameter from this channel definition
	 * 
	 * @param name
	 *            the parameter name
	 */
	public void removeParameter(String name);

	@Deprecated
	public Element getDocument(Document doc, String idTag, String statusMsg,
			int errorId);

	/**
	 * return an xml representation of this channel
	 */
	@Deprecated
	public Element getDocument(Document doc, String idTag);

}