/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channel;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.AbstractChannelRegistryStore;
import org.jasig.portal.channel.dao.IChannelDefinitionDao;
import org.jasig.portal.channel.dao.IChannelTypeDao;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.jasig.portal.portlet.registry.IPortletDefinitionRegistry;

/**
 * JpaChannelRegistryStore is a JPA/Hibernate implementation of the 
 * IChannelRegistryStore interface.  This implementation currently handles
 * IChannelDefinition and IChannelType persistence, while leaving GAP-based
 * category methods to the parent abstract class.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @revision $Revision$
 */
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
    	IChannelDefinition def = channelDao.getChannelDefinition(channelId);
    	if (def != null) {
    		def = completeChannelDefinition(def);
    	}
    	return def;
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinition(java.lang.String)
     */
    public IChannelDefinition getChannelDefinition(String fname) {
    	IChannelDefinition def = channelDao.getChannelDefinition(fname);
    	if (def != null) {
    		def = completeChannelDefinition(def);
    	}
    	return def;
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
    	
    	// if this channel is a portlet, save the associated portlet parameters
        if (channelDef.isPortlet()) {
            //Get or Create the portlet definition
            final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getOrCreatePortletDefinition(channelId);
            
            //Update the preferences of the portlet definition
            final IPortletPreferences portletPreferences = portletDefinition.getPortletPreferences();
            portletPreferences.setPortletPreferences(Arrays.asList(channelDef.getPortletPreferences()));
            this.portletDefinitionRegistry.updatePortletDefinition(portletDefinition);
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

    
    // Internal methods

    /**
     * Temporary internal method for adding the portlet definition to a channel.
     * Eventually this association will be managed via Hibernate.
     * 
     * @param def  ChannelDefinition
     * @return     completed ChannelDefinition with attached portlet preferences
     */
    protected IChannelDefinition completeChannelDefinition(IChannelDefinition def) {
        if (def.isPortlet()) {
            final IPortletDefinition portletDefinition = this.portletDefinitionRegistry.getPortletDefinition(def.getId());
            if (portletDefinition != null) {
                final IPortletPreferences portletPreferences = portletDefinition.getPortletPreferences();
                final List<IPortletPreference> portletPreferencesList = portletPreferences.getPortletPreferences();
                def.replacePortletPreference(portletPreferencesList);
            }
            else {
                log.warn("ChannelDefinition.isPortlet() reports true but no IPortletDefinition exists for channel publish id '" + def.getId() + "'. This channel may not function correctly.");
            }
        }
        return def;
    }

}
