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

/** An interface managing information contained in a user layout channel node. */
public interface IUserLayoutChannelDescription extends IUserLayoutNodeDescription {

    /**
     * Determine if the channel supports "about" action.
     *
     * @return value of hasAbout.
     */
    boolean hasAbout();

    /**
     * Specify whether the channel supports "about" action.
     *
     * @param v Value to assign to hasAbout.
     */
    void setHasAbout(boolean v);

    /**
     * Determine if the channel supports "help" action.
     *
     * @return value of hasHelp.
     */
    boolean hasHelp();

    /**
     * Specify whether the channel supports "help" action.
     *
     * @param v Value to assign to hasHelp.
     */
    void setHasHelp(boolean v);

    /**
     * Determine if the channel is editable.
     *
     * @return value of editable.
     */
    boolean isEditable();

    /**
     * Specify whether the channel is editable.
     *
     * @param v Value to assign to editable.
     */
    void setEditable(boolean v);

    /**
     * Get the value of channel timeout in milliseconds.
     *
     * @return value of timeout.
     */
    long getTimeout();

    /**
     * Set the value of channel timeout in milliseconds.
     *
     * @param v Value to assign to timeout.
     */
    void setTimeout(long v);

    /**
     * Get the value of functionalName.
     *
     * @return value of functionalName.
     */
    String getFunctionalName();

    /**
     * Set the value of functionalName.
     *
     * @param v Value to assign to functionalName.
     */
    void setFunctionalName(String v);

    /**
     * Get the value of channelSubscribeId.
     *
     * @return value of channelSubscribeId.
     */
    String getChannelSubscribeId();

    /**
     * Set the value of channelSubscribeId.
     *
     * @param v Value to assign to channelSubscribeId.
     */
    void setChannelSubscribeId(String v);

    /**
     * Get the value of channelTypeId.
     *
     * @return value of channelTypeId.
     */
    String getChannelTypeId();

    /**
     * Set the value of channelTypeId.
     *
     * @param v Value to assign to channelTypeId.
     */
    void setChannelTypeId(String v);

    /**
     * Get the value of channelPublishId for this channel.
     *
     * @return value of channelPublishId.
     */
    String getChannelPublishId();

    /**
     * Set the value of channelPublishId for this channel.
     *
     * @param v Value to assign to channelPublishId.
     */
    void setChannelPublishId(String v);

    /**
     * Get the value of className implementing this channel.
     *
     * @return value of className.
     */
    String getClassName();

    /**
     * Set the value of className implementing this channel.
     *
     * @param v Value to assign to className.
     */
    void setClassName(String v);

    /**
     * Get the value of title.
     *
     * @return value of title.
     */
    String getTitle();

    /**
     * Set the value of title.
     *
     * @param v Value to assign to title.
     */
    void setTitle(String v);

    /**
     * Get the value of description.
     *
     * @return value of description.
     */
    String getDescription();

    /**
     * Set the value of description.
     *
     * @param v Value to assign to description.
     */
    void setDescription(String v);

    /**
     * Get the value of secure.
     *
     * @return value of secure.
     */
    boolean isSecure();

    /**
     * Set the value of secure.
     *
     * @param v Value to assign to secure.
     */
    void setIsSecure(boolean v);

    /**
     * Return true if the described channel is a JSR-168 portlet, false otherwise.
     *
     * @return true if the described channel is a JSR-168 portlet, false otherwise
     * @deprecated everything is a portlet now
     */
    @Deprecated
    boolean isPortlet();

    // channel parameter methods

    /**
     * Set a channel parameter value.
     *
     * @param parameterValue a <code>String</code> value
     * @param parameterName a <code>String</code> value
     * @return a <code>String</code> value that was set.
     */
    String setParameterValue(String parameterName, String parameterValue);

    /**
     * Obtain a channel parameter value.
     *
     * @param parameterName a <code>String</code> value
     * @return a <code>String</code> value
     */
    String getParameterValue(String parameterName);

    /**
     * Obtain values of all existing channel parameters.
     *
     * @return a <code>Collection</code> of <code>String</code> parameter values.
     */
    Collection getParameterValues();

    /**
     * Obtain a set of channel parameter names.
     *
     * @return a <code>Set</code> of <code>String</code> parameter names.
     */
    Enumeration getParameterNames();

    /**
     * Returns an entire mapping of parameters.
     *
     * @return a <code>Map</code> of parameter names on parameter values.
     */
    Map getParameterMap();

    /**
     * Creates a <code>org.w3c.dom.Element</code> representation of the current channel.
     *
     * @param root a <code>Document</code> for which the <code>Element</code> should be created.
     * @return a <code>Node</code> value
     */
    @Override
    Element getXML(Document root);
};
