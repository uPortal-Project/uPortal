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

package org.jasig.portal.mock.portlet.om;

import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreferences;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MockPortletEntity implements IPortletEntity {
    private static final long serialVersionUID = 1L;

    private IPortletEntityId portletEntityId;
    private String channelSubscribeId;
    private int userId;
    private IPortletDefinitionId portletDefinitionId;
    private IPortletPreferences portletPreferences;
    
    public MockPortletEntity() {
        this.portletEntityId = null;
        this.channelSubscribeId = null;
        this.userId = -1;
        this.portletDefinitionId = null;
        this.portletPreferences = null;
    }

    /**
     * @return the portletEntityId
     */
    public IPortletEntityId getPortletEntityId() {
        return this.portletEntityId;
    }

    /**
     * @param portletEntityId the portletEntityId to set
     */
    public void setPortletEntityId(IPortletEntityId portletEntityId) {
        this.portletEntityId = portletEntityId;
    }

    /**
     * @return the channelSubscribeId
     */
    public String getChannelSubscribeId() {
        return this.channelSubscribeId;
    }

    /**
     * @param channelSubscribeId the channelSubscribeId to set
     */
    public void setChannelSubscribeId(String channelSubscribeId) {
        this.channelSubscribeId = channelSubscribeId;
    }

    /**
     * @return the userId
     */
    public int getUserId() {
        return this.userId;
    }

    /**
     * @param userId the userId to set
     */
    public void setUserId(int userId) {
        this.userId = userId;
    }

    /**
     * @return the portletDefinitionId
     */
    public IPortletDefinitionId getPortletDefinitionId() {
        return this.portletDefinitionId;
    }

    /**
     * @param portletDefinitionId the portletDefinitionId to set
     */
    public void setPortletDefinitionId(IPortletDefinitionId portletDefinitionId) {
        this.portletDefinitionId = portletDefinitionId;
    }

    /**
     * @return the portletPreferences
     */
    public IPortletPreferences getPortletPreferences() {
        return this.portletPreferences;
    }

    /**
     * @param portletPreferences the portletPreferences to set
     */
    public void setPortletPreferences(IPortletPreferences portletPreferences) {
        this.portletPreferences = portletPreferences;
    }
}
