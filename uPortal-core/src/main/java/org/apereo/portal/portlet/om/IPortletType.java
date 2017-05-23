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

import org.apereo.portal.io.xml.IPortalData;

/**
 * A channel type references a particular java class that implements the IChannel interface. It also
 * references a channel publishing document that describes the parameters that must be fed to the
 * channel.
 *
 */
public interface IPortletType extends IPortalData {

    // Getter methods

    /**
     * Get the unique ID of this channel type.
     *
     * @return unique id
     */
    public int getId();

    /**
     * Get the name of this channel type
     *
     * @return channel type name
     */
    public String getName();

    /**
     * Get a description of this channel type
     *
     * @return channel type description
     */
    public String getDescription();

    /**
     * Get the URI of the ChannelPublishingDocument associated with this channel type. This CPD will
     * be used to determine configuration options for channels of this type.
     *
     * @return ChannelPublishingDocument URI
     */
    public String getCpdUri();

    // Setter methods

    /**
     * Set the description for this channel type
     *
     * @param descr
     */
    public void setDescription(String descr);

    /**
     * Set the URI of the ChannelPublishingDocument associated with this channel type. This CPD will
     * be used to determine configuration options for channels of this type.
     *
     * @param cpdUri ChannelPublishingDocument URI
     */
    public void setCpdUri(String cpdUri);
}
