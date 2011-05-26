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

/**
 * IChannelParameter represents an interface for ChannelDefinition parameters.
 * These parameters function as defaults for the channel and may optionally
 * be overridden by end users.
 * 
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 */
public interface IPortletDefinitionParameter {
    
    // Getter methods
    
    /**
     * Get the name of the channel parameter.
     * @return the name of the channel parameter.
     */
    public String getName();
    
    /**
     * Get the default value of the channel parameter.
     * @return the default value for this channel parameter.
     */
    public String getValue();
    
    /**
     * Get a description of this channel parameter.
     * @return a description of this channel parameter.
     */
    public String getDescription();

    
    // Setter methods
    
    /**
     * Set the default value for this channel parameter.
     * @param value the default value for this channel parameter.
     */
    public void setValue(String value);
    
    /**
     * Set the description of this channel parameter.
     * @param descr description of this channel parameter.
     */
    public void setDescription(String descr);

}
