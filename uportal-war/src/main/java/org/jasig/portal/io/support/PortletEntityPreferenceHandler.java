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

package org.jasig.portal.io.support;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.dao.IPortletEntityDao;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataRetrievalFailureException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;

/**
 * Import/Export helper bean for dealing with portlet entity preferences
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
@Service("portletEntityPreferenceHandler")
public class PortletEntityPreferenceHandler {
    private IPortletDefinitionDao portletDefinitionDao;
    private IPortletEntityDao portletEntityDao;
    
    @Autowired
    public void setPortletDefinitionDao(IPortletDefinitionDao portletDefinitionDao) {
        this.portletDefinitionDao = portletDefinitionDao;
    }
    @Autowired
    public void setPortletEntityDao(@Qualifier("transient") IPortletEntityDao portletEntityDao) {
        this.portletEntityDao = portletEntityDao;
    }
    
    public Set<IPortletEntity> getEntityPreferences(Integer userId) {
        final Set<IPortletEntity> portletEntities = this.portletEntityDao.getPortletEntitiesForUser(userId);
        
        for (final Iterator<IPortletEntity> entityItr = portletEntities.iterator(); entityItr.hasNext(); ) {
            final IPortletEntity portletEntity = entityItr.next();
            final List<IPortletPreference> preferencesList = portletEntity.getPortletPreferences();
            
            //Only bother with entities that have preferences
            if (preferencesList.size() <= 0) {
                entityItr.remove();
            }
        }
        
        return portletEntities;
    }

    public void setEntityPreference(String fName, String channelSubscribeId, Integer userId, LinkedHashMap<String, List<String>> preferencesMap) {
        final IPortletEntity portletEntity = this.getPortletEntity(fName, channelSubscribeId, userId);
        
        //Convert the Map to a List of IPortletPreference objects
        final List<IPortletPreference> portletPreferencesList = new ArrayList<IPortletPreference>(preferencesMap.size());
        for (final Entry<String, List<String>> prefEntry : preferencesMap.entrySet()) {
            final String prefName = prefEntry.getKey();
            final List<String> prefValues = prefEntry.getValue();
            
            final IPortletPreference portletPreference = new PortletPreferenceImpl(prefName, false, prefValues.toArray(new String[prefValues.size()]));
            portletPreferencesList.add(portletPreference);
        }
        
        //Persist the changes.
        portletEntity.setPortletPreferences(portletPreferencesList);
        this.portletEntityDao.updatePortletEntity(portletEntity);
    }
    
    protected IPortletEntity getPortletEntity(String fName, String layoutNodeId, int userId) {
        //Load the channel definition
        final IPortletDefinition portletDefinition;
        try {
            portletDefinition = this.portletDefinitionDao.getPortletDefinitionByFname(fName);
        }
        catch (Exception e) {
            throw new DataRetrievalFailureException("Failed to retrieve ChannelDefinition for fName='" + fName + "'", e);
        }
        
        //The channel definition for the fName MUST exist for this class to function
        if (portletDefinition == null) {
            throw new EmptyResultDataAccessException("No ChannelDefinition exists for fName='" + fName + "'", 1);
        }
        
        //get/create the portlet entity
        final IPortletEntity portletEntity = this.portletEntityDao.getPortletEntity(layoutNodeId, userId);
        if (portletEntity != null) {
            return portletEntity;
        }
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
        return this.portletEntityDao.createPortletEntity(portletDefinitionId, layoutNodeId, userId);
    }
}
