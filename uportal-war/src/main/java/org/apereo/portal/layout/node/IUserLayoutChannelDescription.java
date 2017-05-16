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
package org.apereo.portal.layout.node;

import java.util.Collection;
import java.util.Enumeration;
import java.util.Map;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * An interface managing information contained in a user layout channel node.
 */
public interface IUserLayoutChannelDescription extends IUserLayoutNodeDescription {

    /**
     * Determine if the channel supports "about" action.
     *
     * @return value of hasAbout.
     */
    public boolean hasAbout();

    /**
     * Specify whether the channel supports "about" action.
     *
     * @param v Value to assign to hasAbout.
     */
    public void setHasAbout(boolean v);

    /**
     * Determine if the channel supports "help" action.
     *
     * @return value of hasHelp.
     */
    public boolean hasHelp();

    /**
     * Specify whether the channel supports "help" action.
     *
     * @param v Value to assign to hasHelp.
     */
    public void setHasHelp(boolean v);

    /**
     * Determine if the channel is editable.
     *
     * @return value of editable.
     */
    public boolean isEditable();

    /**
     * Specify whether the channel is editable.
     *
     * @param v Value to assign to editable.
     */
    public void setEditable(boolean v);

    /**
     * Get the value of channel timeout in milliseconds.
     *
     * @return value of timeout.
     */
    public long getTimeout();

    /**
     * Set the value of channel timeout in milliseconds.
     *
     * @param v Value to assign to timeout.
     */
    public void setTimeout(long v);

    /**
     * Get the value of functionalName.
     *
     * @return value of functionalName.
     */
    public String getFunctionalName();

    /**
     * Set the value of functionalName.
     *
     * @param v Value to assign to functionalName.
     */
    public void setFunctionalName(String v);

    /**
     * Get the value of channelSubscribeId.
     *
     * @return value of channelSubscribeId.
     */
    public String getChannelSubscribeId();

    /**
     * Set the value of channelSubscribeId.
     *
     * @param v Value to assign to channelSubscribeId.
     */
    public void setChannelSubscribeId(String v);

    /**
     * Get the value of channelTypeId.
     *
     * @return value of channelTypeId.
     */
    public String getChannelTypeId();

    /**
     * Set the value of channelTypeId.
     *
     * @param v Value to assign to channelTypeId.
     */
    public void setChannelTypeId(String v);

    /**
     * Get the value of channelPublishId for this channel.
     *
     * @return value of channelPublishId.
     */
    public String getChannelPublishId();

    /**
     * Set the value of channelPublishId for this channel.
     *
     * @param v Value to assign to channelPublishId.
     */
    public void setChannelPublishId(String v);

    /**
     * Get the value of className implementing this channel.
     *
     * @return value of className.
     */
    public String getClassName();

    /**
     * Set the value of className implementing this channel.
     *
     * @param v Value to assign to className.
     */
    public void setClassName(String v);

    /**
     * Get the value of title.
     *
     * @return value of title.
     */
    public String getTitle();

    /**
     * Set the value of title.
     *
     * @param v Value to assign to title.
     */
    public void setTitle(String v);

    /**
     * Get the value of description.
     *
     * @return value of description.
     */
    public String getDescription();

    /**
     * Set the value of description.
     *
     * @param v Value to assign to description.
     */
    public void setDescription(String v);

    /**
     * Get the value of secure.
     *
     * @return value of secure.
     */
    public boolean isSecure();

    /**
     * Set the value of secure.
     *
     * @param v Value to assign to secure.
     */
    public void setIsSecure(boolean v);

    /**
     * Return true if the described channel is a JSR-168 portlet, false otherwise.
     *
     * @return true if the described channel is a JSR-168 portlet, false otherwise
     * @deprecated everything is a portlet now
     */
    @Deprecated
    public boolean isPortlet();

    // channel parameter methods

    /**
     * Set a channel parameter value.
     *
     * @param parameterValue a <code>String</code> value
     * @param parameterName a <code>String</code> value
     * @return a <code>String</code> value that was set.
     */
    public String setParameterValue(String parameterName, String parameterValue);

    /**
     * Obtain a channel parameter value.
     *
     * @param parameterName a <code>String</code> value
     * @return a <code>String</code> value
     */
    public String getParameterValue(String parameterName);

    /**
     * Obtain values of all existing channel parameters.
     *
     * @return a <code>Collection</code> of <code>String</code> parameter values.
     */
    public Collection getParameterValues();

    /**
     * Obtain a set of channel parameter names.
     *
     * @return a <code>Set</code> of <code>String</code> parameter names.
     */
    public Enumeration getParameterNames();

    /**
     * Returns an entire mapping of parameters.
     *
     * @return a <code>Map</code> of parameter names on parameter values.
     */
    public Map getParameterMap();

    /**
     * Creates a <code>org.w3c.dom.Element</code> representation of the current channel.
     *
     * @param root a <code>Document</code> for which the <code>Element</code> should be created.
     * @return a <code>Node</code> value
     */
    public Element getXML(Document root);
};
