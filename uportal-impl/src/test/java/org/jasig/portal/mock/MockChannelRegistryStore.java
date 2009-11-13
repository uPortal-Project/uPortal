/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.mock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jasig.portal.ChannelCategory;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.channel.IChannelType;
import org.jasig.portal.security.IPerson;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MockChannelRegistryStore implements IChannelRegistryStore {
    private Map<Integer, IChannelDefinition> channelDefinitionsById = Collections.emptyMap();

    public void setChannelDefinitions(IChannelDefinition[] channelDefinitions) {
        this.channelDefinitionsById = new HashMap<Integer, IChannelDefinition>(channelDefinitions.length);
        
        for (IChannelDefinition channelDefinition : channelDefinitions) {
            this.channelDefinitionsById.put(channelDefinition.getId(), channelDefinition);
        }
    }
    
    


    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinition(int)
     */
    public IChannelDefinition getChannelDefinition(int channelPublishId) {
        return this.channelDefinitionsById.get(channelPublishId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinitions()
     */
    public List<IChannelDefinition> getChannelDefinitions() {
    	List<IChannelDefinition> defs = new ArrayList<IChannelDefinition>();
    	defs.addAll(this.channelDefinitionsById.values());
        return defs;
    }
    


    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#addCategoryToCategory(org.jasig.portal.ChannelCategory, org.jasig.portal.ChannelCategory)
     */
    public void addCategoryToCategory(ChannelCategory source, ChannelCategory destination) {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#addChannelToCategory(org.jasig.portal.ChannelDefinition, org.jasig.portal.ChannelCategory)
     */
    public void addChannelToCategory(IChannelDefinition channelDef, ChannelCategory category) {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#approveChannelDefinition(org.jasig.portal.ChannelDefinition, org.jasig.portal.security.IPerson, java.util.Date)
     */
    public void approveChannelDefinition(IChannelDefinition channelDef, IPerson approver, Date approveDate)
            {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#deleteChannelCategory(org.jasig.portal.ChannelCategory)
     */
    public void deleteChannelCategory(ChannelCategory category) {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#deleteChannelDefinition(org.jasig.portal.ChannelDefinition)
     */
    public void deleteChannelDefinition(IChannelDefinition channelDef) {
        throw new UnsupportedOperationException("Not Implemented");

    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelType(java.lang.String)
     */
    public IChannelType getChannelType(String name) {

        throw new UnsupportedOperationException("Not Implemented");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getOrCreateChannelType(java.lang.String, java.lang.String, java.lang.String)
     */
    public IChannelType getOrCreateChannelType(String name, String clazz, String cpdUri) {

        throw new UnsupportedOperationException("Not Implemented");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#deleteChannelType(org.jasig.portal.ChannelType)
     */
    public void deleteChannelType(IChannelType chanType) {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#disapproveChannelDefinition(org.jasig.portal.ChannelDefinition)
     */
    public void disapproveChannelDefinition(IChannelDefinition channelDef) {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getAllChildCategories(org.jasig.portal.ChannelCategory)
     */
    public ChannelCategory[] getAllChildCategories(ChannelCategory parent) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getAllChildChannels(org.jasig.portal.ChannelCategory)
     */
    public IChannelDefinition[] getAllChildChannels(ChannelCategory parent) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getAllChildChannels(org.jasig.portal.ChannelCategory,org.jasig.portal.security.IPerson)
     */
	public IChannelDefinition[] getAllChildChannels(ChannelCategory parent,
			IPerson person) {
        throw new UnsupportedOperationException("Not Implemented");
	}

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelCategory(java.lang.String)
     */
    public ChannelCategory getChannelCategory(String channelCategoryId) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinition(java.lang.String)
     */
    public IChannelDefinition getChannelDefinition(String channelFunctionalName) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinitionByName(java.lang.String)
     */
    public IChannelDefinition getChannelDefinitionByName(String channelName) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelType(int)
     */
    public IChannelType getChannelType(int channelTypeId) {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelTypes()
     */
    public List<IChannelType> getChannelTypes() {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChildCategories(org.jasig.portal.ChannelCategory)
     */
    public ChannelCategory[] getChildCategories(ChannelCategory parent) {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChildChannels(org.jasig.portal.ChannelCategory)
     */
    public IChannelDefinition[] getChildChannels(ChannelCategory parent) {
        throw new UnsupportedOperationException("Not Implemented");
        
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChildChannels(org.jasig.portal.ChannelCategory,org.jasig.portal.security.IPerson)
     */
	public IChannelDefinition[] getChildChannels(ChannelCategory parent,
			IPerson person) {
        throw new UnsupportedOperationException("Not Implemented");
        
	}

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getManageableChildChannels(org.jasig.portal.ChannelCategory,org.jasig.portal.security.IPerson)
     */
	public IChannelDefinition[] getManageableChildChannels(
			ChannelCategory parent, IPerson person) {
        throw new UnsupportedOperationException("Not Implemented");
        
	}
	
    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getAllManageableChildChannels(org.jasig.portal.ChannelCategory,org.jasig.portal.security.IPerson)
     */
	public IChannelDefinition[] getAllManageableChildChannels(
			ChannelCategory parent, IPerson person) {
        throw new UnsupportedOperationException("Not Implemented"); 
	}

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getParentCategories(org.jasig.portal.ChannelCategory)
     */
    public ChannelCategory[] getParentCategories(ChannelCategory child) {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getParentCategories(org.jasig.portal.ChannelDefinition)
     */
    public ChannelCategory[] getParentCategories(IChannelDefinition child) {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getTopLevelChannelCategory()
     */
    public ChannelCategory getTopLevelChannelCategory() {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#newChannelCategory()
     */
    public ChannelCategory newChannelCategory() {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#newChannelCategory(java.lang.String, java.lang.String, java.lang.String)
     */
    public ChannelCategory newChannelCategory(String name, String description, String creatorId) {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#newChannelDefinition(int, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    public IChannelDefinition newChannelDefinition(int channelTypeId, String fname, String clazz, String name, String title) {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#newChannelType(java.lang.String, java.lang.String, java.lang.String)
     */
    public IChannelType newChannelType(String name, String clazz, String cpdUri) {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#removeCategoryFromCategory(org.jasig.portal.ChannelCategory, org.jasig.portal.ChannelCategory)
     */
    public void removeCategoryFromCategory(ChannelCategory child, ChannelCategory parent) {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#removeChannelFromCategory(org.jasig.portal.ChannelDefinition, org.jasig.portal.ChannelCategory)
     */
    public void removeChannelFromCategory(IChannelDefinition channelDef, ChannelCategory category) {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#saveChannelCategory(org.jasig.portal.ChannelCategory)
     */
    public void saveChannelCategory(ChannelCategory category) {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#saveChannelDefinition(org.jasig.portal.ChannelDefinition)
     */
    public void saveChannelDefinition(IChannelDefinition channelDef) {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#saveChannelType(org.jasig.portal.ChannelType)
     */
    public IChannelType saveChannelType(IChannelType chanType) {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /*
     * (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinitions(org.jasig.portal.security.IPerson)
     */
	public List<IChannelDefinition> getChannelDefinitions(
			IPerson person) {
        throw new UnsupportedOperationException("Not Implemented");
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IChannelRegistryStore#getViewableChannelDefinitionsForUser(org.jasig.portal.security.IPerson)
	 */
	public List<IChannelDefinition> getViewableChannelDefinitionsForUser(
			IPerson person) {
        throw new UnsupportedOperationException("Not Implemented");
	}

}
