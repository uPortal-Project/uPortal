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

package org.jasig.portal.portlet.registry;

import java.util.List;

import org.jasig.portal.portlet.om.IPortletType;

public interface IPortletTypeRegistry {

    public IPortletType createPortletType(String name, String cpdUri);
    
    public IPortletType getPortletType(int channelTypeId);

    public IPortletType getPortletType(String name);

    public List<IPortletType> getPortletTypes();
    
    public IPortletType savePortletType(IPortletType chanType);
    
    public void deleteChannelType(IPortletType chanType);

}
