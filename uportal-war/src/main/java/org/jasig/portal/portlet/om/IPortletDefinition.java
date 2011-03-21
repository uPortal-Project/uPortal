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

package org.jasig.portal.portlet.om;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.io.xml.IPortalData;


/**
 * A portlet definition is equivalant to a published ChannelDefinition. 
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IPortletDefinition extends IBasicEntity, IPortalData {
    public static final String EDITABLE_PARAM = "editable";
    public static final String HAS_HELP_PARAM = "hasHelp";
    public static final String HAS_ABOUT_PARAM = "hasAbout";
    
    /**
     * @return The unique identifier for this portlet definition.
     */
    public IPortletDefinitionId getPortletDefinitionId();
    
    /**
     * @return The preferences for this portlet definition, will not be null.
     */
    public IPortletPreferences getPortletPreferences();

    public void setPortletPreferences(List<IPortletPreference> portletPreferences);

    /**
     * @param portletPreferences The preferences for this portlet definition.
     * @throws IllegalArgumentException If preferences is null.
     */
    public void setPortletPreferences(IPortletPreferences portletPreferences);
    
    public PortletLifecycleState getLifecycleState();
    
	public String getFName();

	public String getName();

	public String getDescription();
	
	public IPortletDescriptorKey getPortletDescriptorKey();
	
	public String getTitle();

	public int getTimeout();

	public IPortletType getType();

	public int getPublisherId();

	public int getApproverId();

	public Date getPublishDate();

	public Date getApprovalDate();
	
	public int getExpirerId();
	
	public Date getExpirationDate();

	public Set<IPortletDefinitionParameter> getParameters();

	public IPortletDefinitionParameter getParameter(String key);

	public Map<String, IPortletDefinitionParameter> getParametersAsUnmodifiableMap();

	// I18n
	public String getName(String locale);

	public String getDescription(String locale);

	public String getTitle(String locale);

	// Setter methods
	public void setFName(String fname);

	public void setName(String name);

	public void setDescription(String descr);

	public void setTitle(String title);

	public void setTimeout(int timeout);

	public void setType(IPortletType channelType);

	public void setPublisherId(int publisherId);

	public void setApproverId(int approvalId);

	public void setPublishDate(Date publishDate);

	public void setApprovalDate(Date approvalDate);

	public void setExpirerId(int expirerId);
	
	public void setExpirationDate(Date expirationDate);

	public void clearParameters();

	public void setParameters(Set<IPortletDefinitionParameter> parameters);

	public void replaceParameters(Set<IPortletDefinitionParameter> parameters);

	public void addLocalizedTitle(String locale, String chanTitle);

	public void addLocalizedName(String locale, String chanName);

	public void addLocalizedDescription(String locale, String chanDesc);
	
	/**
	 * Implementation required by IBasicEntity interface.
	 * 
	 * @return EntityIdentifier
	 */
	@Override
    public EntityIdentifier getEntityIdentifier();

	/**
	 * Adds a parameter to this channel definition
	 * 
	 * @param parameter
	 *            the channel parameter to add
	 */
	public void addParameter(IPortletDefinitionParameter parameter);

	public void addParameter(String name, String value);

	/**
	 * Removes a parameter from this channel definition
	 * 
	 * @param parameter
	 *            the channel parameter to remove
	 */
	public void removeParameter(IPortletDefinitionParameter parameter);

	/**
	 * Removes a parameter from this channel definition
	 * 
	 * @param name
	 *            the parameter name
	 */
	public void removeParameter(String name);

}
