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

package org.jasig.portal.channel;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AbstractChannelRegistryStore;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.channel.dao.IChannelDefinitionDao;
import org.jasig.portal.channel.dao.IChannelTypeDao;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * JpaChannelRegistryStore is a JPA/Hibernate implementation of the 
 * IChannelRegistryStore interface.  This implementation currently handles
 * IChannelDefinition and IChannelType persistence, while leaving GAP-based
 * category methods to the parent abstract class.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 * @deprecated IChannel rendering code will be replaced with portlet specific rendering code in a future release
 */
@Deprecated
public final class JpaChannelRegistryStore extends AbstractChannelRegistryStore {
    
    private IChannelDefinitionDao channelDao;
    
    /**
     * Set the dao for channel definition persistence.
     * 
     * @param channelDao
     */
	public void setChannelDao(IChannelDefinitionDao channelDao) {
		this.channelDao = channelDao;
	}

    private IChannelTypeDao channelTypeDao;
    
    /**
     * Set the dao for channel type persistence.
     * 
     * @param channelTypeDao
     */
	public void setChannelTypeDao(IChannelTypeDao channelTypeDao) {
		this.channelTypeDao = channelTypeDao;
	}

	private IPortletDefinitionRegistry portletDefinitionRegistry;
    
	/**
	 * Set the portlet definition registry.
	 * 
	 * @param portletDefinitionRegistry
	 */
	@Autowired(required=true)
	public void setPortletDefinitionRegistry(
			IPortletDefinitionRegistry portletDefinitionRegistry) {
		this.portletDefinitionRegistry = portletDefinitionRegistry;
	}

	private Log log = LogFactory.getLog(JpaChannelRegistryStore.class);
    
	
    // Public ChannelDefinition methods
	
    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#newChannelDefinition(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public IChannelDefinition newChannelDefinition(int channelTypeId, String fname, String clazz, String name, String title) {
        final IChannelType channelType = this.getChannelType(channelTypeId);
        if (channelType == null) {
            throw new IllegalArgumentException("No IChannelType exists for id " + channelTypeId);
        }
        
        return this.channelDao.createChannelDefinition(channelType, fname, clazz, name, title);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinition(int)
     */
    public IChannelDefinition getChannelDefinition(int channelId) {
    	return channelDao.getChannelDefinition(channelId);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinition(java.lang.String)
     */
    public IChannelDefinition getChannelDefinition(String fname) {
    	return channelDao.getChannelDefinition(fname);
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinitionByName(java.lang.String)
     */
    public IChannelDefinition getChannelDefinitionByName(String name) {
    	return channelDao.getChannelDefinitionByName(name);
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#saveChannelDefinition(org.jasig.portal.channel.IChannelDefinition)
     */
    public void saveChannelDefinition(IChannelDefinition channelDef) {
    	int channelId = channelDef.getId();
    	channelDao.updateChannelDefinition(channelDef);
    	if (channelId < 0) {
    		IChannelDefinition newChannel = getChannelDefinition(channelDef.getFName());
    		channelId = newChannel.getId();
    	}
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#deleteChannelDefinition(org.jasig.portal.channel.IChannelDefinition)
     */
    public void deleteChannelDefinition(IChannelDefinition channelDef) {
    	channelDao.deleteChannelDefinition(channelDef);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinitions()
     */
    public List<IChannelDefinition> getChannelDefinitions() {
    	List<IChannelDefinition> defs = channelDao.getChannelDefinitions();
    	return defs;
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinitions(org.jasig.portal.security.IPerson)
     */
    public List<IChannelDefinition> getChannelDefinitions(IPerson person) {
    	
        EntityIdentifier ei = person.getEntityIdentifier();
        IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
    	
        List<IChannelDefinition> defs = channelDao.getChannelDefinitions();
        List<IChannelDefinition> manageableChannels = new ArrayList<IChannelDefinition>();
        
    	for(IChannelDefinition def : defs) {
    		if(ap.canSubscribe(def.getId())) {
    			manageableChannels.add(def);
    		}
    	}
    	
    	return manageableChannels;
    }
    
    // Public ChannelType methods
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#newChannelType()
     */
    public IChannelType newChannelType(String name, String clazz, String cpdUri) {
    	return this.channelTypeDao.createChannelType(name, clazz, cpdUri);
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getOrCreateChannelType(java.lang.String, java.lang.String, java.lang.String)
     */
    public IChannelType getOrCreateChannelType(String name, String clazz, String cpdUri) {
        final IChannelType channelType = this.getChannelType(name);
        if (channelType != null) {
            return channelType;
        }
        
        return this.newChannelType(name, clazz, cpdUri);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelType(int)
     */
    public IChannelType getChannelType(int channelTypeId) {
    	return channelTypeDao.getChannelType(channelTypeId);
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelType(java.lang.String)
     */
    public IChannelType getChannelType(String name) {
        return channelTypeDao.getChannelType(name);
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelTypes()
     */
    public List<IChannelType> getChannelTypes() {
        return channelTypeDao.getChannelTypes();
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#saveChannelType(org.jasig.portal.channel.IChannelType)
     */
    public IChannelType saveChannelType(IChannelType chanType) {
        return channelTypeDao.updateChannelType(chanType);
    }
    
    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#deleteChannelType(org.jasig.portal.channel.IChannelType)
     */
    public void deleteChannelType(IChannelType chanType) {
    	channelTypeDao.deleteChannelType(chanType);
    }

}
