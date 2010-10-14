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
import java.util.Set;
import java.util.Map.Entry;

import javax.sql.DataSource;

import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.portlet.registry.IPortletEntityRegistry;
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
    private IChannelRegistryStore channelRegistryStore;
    private IPortletDefinitionRegistry portletDefinitionRegistry;
    private IPortletEntityRegistry portletEntityRegistry;
    private DataSource dataSource;
    
    /**
     * @return the channelRegistryStore
     */
    public IChannelRegistryStore getChannelRegistryStore() {
        return channelRegistryStore;
    }
    /**
     * @param channelRegistryStore the channelRegistryStore to set
     */
    @Autowired
    public void setChannelRegistryStore(IChannelRegistryStore channelRegistryStore) {
        this.channelRegistryStore = channelRegistryStore;
    }
    /**
     * @return the portletDefinitionRegistry
     */
    public IPortletDefinitionRegistry getPortletDefinitionRegistry() {
        return portletDefinitionRegistry;
    }
    /**
     * @param portletDefinitionRegistry the portletDefinitionRegistry to set
     */
    @Autowired
    public void setPortletDefinitionRegistry(IPortletDefinitionRegistry portletDefinitionRegistry) {
        this.portletDefinitionRegistry = portletDefinitionRegistry;
    }
    /**
     * @return the portletEntityRegistry
     */
    public IPortletEntityRegistry getPortletEntityRegistry() {
        return portletEntityRegistry;
    }
    /**
     * @param portletEntityRegistry the portletEntityRegistry to set
     */
    @Autowired
    public void setPortletEntityRegistry(IPortletEntityRegistry portletEntityRegistry) {
        this.portletEntityRegistry = portletEntityRegistry;
    }
    /**
     * @return the dataSource
     */
    public DataSource getDataSource() {
        return dataSource;
    }
    /**
     * @param dataSource the dataSource to set
     */
    @Autowired
    public void setDataSource(@Qualifier("PortalDb") DataSource dataSource) {
        this.dataSource = dataSource;
    }
    
    public Set<IPortletEntity> getEntityPreferences(Integer userId) {
        final Set<IPortletEntity> portletEntities = this.portletEntityRegistry.getPortletEntitiesForUser(userId);
        
        for (final Iterator<IPortletEntity> entityItr = portletEntities.iterator(); entityItr.hasNext(); ) {
            final IPortletEntity portletEntity = entityItr.next();
            final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
            final List<IPortletPreference> preferencesList = portletPreferences.getPortletPreferences();
            
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
        final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
        portletPreferences.setPortletPreferences(portletPreferencesList);
        this.portletEntityRegistry.storePortletEntity(portletEntity);
    }
    
    protected IPortletEntity getPortletEntity(String fName, String channelSubscribeId, int userId) {
        //Load the channel definition
        final IChannelDefinition channelDefinition;
        try {
            channelDefinition = this.channelRegistryStore.getChannelDefinition(fName);
        }
        catch (Exception e) {
            throw new DataRetrievalFailureException("Failed to retrieve ChannelDefinition for fName='" + fName + "'", e);
        }
        
        //The channel definition for the fName MUST exist for this class to function
        if (channelDefinition == null) {
            throw new EmptyResultDataAccessException("No ChannelDefinition exists for fName='" + fName + "'", 1);
        }
        
        //get/create the portlet definition
        final int channelDefinitionId = channelDefinition.getId();
        final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getOrCreatePortletDefinition(channelDefinitionId);
        
        //get/create the portlet entity
        final IPortletDefinitionId portletDefinitionId = portletDefinition.getPortletDefinitionId();
        return this.portletEntityRegistry.getOrCreatePortletEntity(portletDefinitionId, channelSubscribeId, userId);
    }
}
