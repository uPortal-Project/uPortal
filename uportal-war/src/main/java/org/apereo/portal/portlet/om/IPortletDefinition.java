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
package org.apereo.portal.portlet.om;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apereo.portal.IBasicEntity;
import org.apereo.portal.io.xml.IPortalData;

/**
 * A portlet definition is equivalent to a published ChannelDefinition.
 *
 */
public interface IPortletDefinition extends IBasicEntity, IPortalData {

    String EDITABLE_PARAM = "editable";
    String CONFIGURABLE_PARAM = "configurable";
    String HAS_HELP_PARAM = "hasHelp";
    String HAS_ABOUT_PARAM = "hasAbout";

    String DISTINGUISHED_GROUP = IPortletDefinition.class.getName();

    /**
     * The name of the portlet parameter that if present represents an alternative URL that ought to
     * be used to "maximize" the defined portlet.
     *
     * <p>This is useful for portlets that when maximized ought to instead be the external URL or
     * web application that they're representing in the portal.
     */
    String ALT_MAX_LINK_PARAM = "alternativeMaximizedLink";

    /** A portlet parameter that specifies a target for the flyout, eg : _blank */
    String TARGET_PARAM = "target";

    /** @return The unique identifier for this portlet definition. */
    IPortletDefinitionId getPortletDefinitionId();

    /** @return The List of PortletPreferences, will not be null */
    List<IPortletPreference> getPortletPreferences();

    /**
     * @param portletPreferences The List of PortletPreferences, null clears the preferences but
     *     actually sets an empty list
     * @return true if the portlet preferences changed
     */
    boolean setPortletPreferences(List<IPortletPreference> portletPreferences);

    PortletLifecycleState getLifecycleState();

    String getFName();

    String getName();

    String getDescription();

    IPortletDescriptorKey getPortletDescriptorKey();

    String getTitle();

    /** @return Default timeout in ms, -1 means no timeout. */
    int getTimeout();
    /**
     * @return Optional timeout for action requests in ms, if null {@link #getTimeout()} should be
     *     used, -1 means no timeout.
     */
    Integer getActionTimeout();
    /**
     * @return Optional timeout for event requests in ms, if null {@link #getTimeout()} should be
     *     used, -1 means no timeout.
     */
    Integer getEventTimeout();
    /**
     * @return Optional timeout for render requests in ms, if null {@link #getTimeout()} should be
     *     used, -1 means no timeout.
     */
    Integer getRenderTimeout();
    /**
     * @return Optional timeout for resource requests in ms, if null {@link #getTimeout()} should be
     *     used, -1 means no timeout.
     */
    Integer getResourceTimeout();

    IPortletType getType();

    int getPublisherId();

    int getApproverId();

    Date getPublishDate();

    Date getApprovalDate();

    int getExpirerId();

    Date getExpirationDate();

    /** @return a READ-ONLY copy of the parameters */
    Set<IPortletDefinitionParameter> getParameters();

    IPortletDefinitionParameter getParameter(String key);

    Map<String, IPortletDefinitionParameter> getParametersAsUnmodifiableMap();

    // I18n
    String getName(String locale);

    String getDescription(String locale);

    String getTitle(String locale);

    /**
     * Returns the alternative maximized link (URL) associated with this portlet definition, or null
     * if none.
     *
     * <p>Syntactic sugar for parsing potential alternative maximized link as a preferable
     * alternative to directly parsing the portlet parameters elsewhere.
     *
     * @return String representing alternative max URL, or null if none.
     * @since 4.2
     */
    String getAlternativeMaximizedLink();

    /**
     * Syntactic sugar for getting the target parameter from the portlet parameters.
     *
     * @return the target tab/window
     */
    String getTarget();

    // Setter methods
    void setFName(String fname);

    void setName(String name);

    void setDescription(String descr);

    void setTitle(String title);

    /** @param timeout The default timeout value in ms, -1 means no timeout. */
    void setTimeout(int timeout);

    /**
     * @param actionTimeout Optional timeout for action requests in ms, if null {@link
     *     #getTimeout()} will be used, -1 means no timeout.
     */
    void setActionTimeout(Integer actionTimeout);
    /**
     * @param eventTimeout Optional timeout for event requests in ms, if null {@link #getTimeout()}
     *     will be used, -1 means no timeout.
     */
    void setEventTimeout(Integer eventTimeout);
    /**
     * @param renderTimeout Optional timeout for render requests in ms, if null {@link
     *     #getTimeout()} will be used, -1 means no timeout.
     */
    void setRenderTimeout(Integer renderTimeout);
    /**
     * @param resourceTimeout Optional timeout for resource requests in ms, if null {@link
     *     #getTimeout()} will be used, -1 means no timeout.
     */
    void setResourceTimeout(Integer resourceTimeout);

    void setType(IPortletType channelType);

    void setPublisherId(int publisherId);

    void setApproverId(int approvalId);

    void setPublishDate(Date publishDate);

    void setApprovalDate(Date approvalDate);

    void setExpirerId(int expirerId);

    void setExpirationDate(Date expirationDate);

    void setParameters(Set<IPortletDefinitionParameter> parameters);

    void addLocalizedTitle(String locale, String chanTitle);

    void addLocalizedName(String locale, String chanName);

    void addLocalizedDescription(String locale, String chanDesc);

    /** @return a portlet rating */
    Double getRating();

    /** @param rating sets portlet rating */
    void setRating(Double rating);

    /** @return Number of users that rated this portlet */
    Long getUsersRated();

    /** @param usersRated sets number of users that rated this portlet */
    void setUsersRated(Long usersRated);

    /**
     * Adds a parameter to this channel definition
     *
     * @param parameter the channel parameter to add
     */
    void addParameter(IPortletDefinitionParameter parameter);

    void addParameter(String name, String value);

    /**
     * Removes a parameter from this channel definition
     *
     * @param parameter the channel parameter to remove
     */
    void removeParameter(IPortletDefinitionParameter parameter);

    /**
     * Removes a parameter from this channel definition
     *
     * @param name the parameter name
     */
    void removeParameter(String name);

    /** @return Hash code based only on the fname of the portlet definition */
    @Override
    int hashCode();

    /**
     * Equals must be able to compare against any other {@link IPortletDefinition} and the
     * comparison must only use the fname
     */
    @Override
    boolean equals(Object o);
}
