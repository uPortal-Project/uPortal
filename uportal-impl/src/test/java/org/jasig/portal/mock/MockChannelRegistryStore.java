/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.mock;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.jasig.portal.ChannelCategory;
import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelType;
import org.jasig.portal.IChannelRegistryStore;
import org.jasig.portal.security.IPerson;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class MockChannelRegistryStore implements IChannelRegistryStore {
    private Map<Integer, ChannelDefinition> channelDefinitionsById = Collections.emptyMap();

    public void setChannelDefinitions(ChannelDefinition[] channelDefinitions) {
        this.channelDefinitionsById = new HashMap<Integer, ChannelDefinition>(channelDefinitions.length);
        
        for (ChannelDefinition channelDefinition : channelDefinitions) {
            this.channelDefinitionsById.put(channelDefinition.getId(), channelDefinition);
        }
    }
    
    


    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinition(int)
     */
    public ChannelDefinition getChannelDefinition(int channelPublishId) throws Exception {
        return this.channelDefinitionsById.get(channelPublishId);
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinitions()
     */
    public ChannelDefinition[] getChannelDefinitions() throws Exception {
        return this.channelDefinitionsById.values().toArray(new ChannelDefinition[0]);
    }
    


    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#addCategoryToCategory(org.jasig.portal.ChannelCategory, org.jasig.portal.ChannelCategory)
     */
    public void addCategoryToCategory(ChannelCategory source, ChannelCategory destination) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#addChannelToCategory(org.jasig.portal.ChannelDefinition, org.jasig.portal.ChannelCategory)
     */
    public void addChannelToCategory(ChannelDefinition channelDef, ChannelCategory category) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#approveChannelDefinition(org.jasig.portal.ChannelDefinition, org.jasig.portal.security.IPerson, java.util.Date)
     */
    public void approveChannelDefinition(ChannelDefinition channelDef, IPerson approver, Date approveDate)
            throws Exception {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#deleteChannelCategory(org.jasig.portal.ChannelCategory)
     */
    public void deleteChannelCategory(ChannelCategory category) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#deleteChannelDefinition(org.jasig.portal.ChannelDefinition)
     */
    public void deleteChannelDefinition(ChannelDefinition channelDef) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#deleteChannelType(org.jasig.portal.ChannelType)
     */
    public void deleteChannelType(ChannelType chanType) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#disapproveChannelDefinition(org.jasig.portal.ChannelDefinition)
     */
    public void disapproveChannelDefinition(ChannelDefinition channelDef) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelCategory(java.lang.String)
     */
    public ChannelCategory getChannelCategory(String channelCategoryId) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelDefinition(java.lang.String)
     */
    public ChannelDefinition getChannelDefinition(String channelFunctionalName) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelType(int)
     */
    public ChannelType getChannelType(int channelTypeId) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChannelTypes()
     */
    public ChannelType[] getChannelTypes() throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChildCategories(org.jasig.portal.ChannelCategory)
     */
    public ChannelCategory[] getChildCategories(ChannelCategory parent) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getChildChannels(org.jasig.portal.ChannelCategory)
     */
    public ChannelDefinition[] getChildChannels(ChannelCategory parent) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getParentCategories(org.jasig.portal.ChannelCategory)
     */
    public ChannelCategory[] getParentCategories(ChannelCategory child) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getParentCategories(org.jasig.portal.ChannelDefinition)
     */
    public ChannelCategory[] getParentCategories(ChannelDefinition child) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#getTopLevelChannelCategory()
     */
    public ChannelCategory getTopLevelChannelCategory() throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#newChannelCategory()
     */
    public ChannelCategory newChannelCategory() throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#newChannelCategory(java.lang.String, java.lang.String, java.lang.String)
     */
    public ChannelCategory newChannelCategory(String name, String description, String creatorId) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#newChannelDefinition()
     */
    public ChannelDefinition newChannelDefinition() throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#newChannelDefinition(int)
     */
    public ChannelDefinition newChannelDefinition(int id) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#newChannelType()
     */
    public ChannelType newChannelType() throws Exception {
        throw new UnsupportedOperationException("Not Implemented");
        
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#removeCategoryFromCategory(org.jasig.portal.ChannelCategory, org.jasig.portal.ChannelCategory)
     */
    public void removeCategoryFromCategory(ChannelCategory child, ChannelCategory parent) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#removeChannelFromCategory(org.jasig.portal.ChannelDefinition, org.jasig.portal.ChannelCategory)
     */
    public void removeChannelFromCategory(ChannelDefinition channelDef, ChannelCategory category) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#saveChannelCategory(org.jasig.portal.ChannelCategory)
     */
    public void saveChannelCategory(ChannelCategory category) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#saveChannelDefinition(org.jasig.portal.ChannelDefinition)
     */
    public void saveChannelDefinition(ChannelDefinition channelDef) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");

    }

    /* (non-Javadoc)
     * @see org.jasig.portal.IChannelRegistryStore#saveChannelType(org.jasig.portal.ChannelType)
     */
    public void saveChannelType(ChannelType chanType) throws Exception {
        throw new UnsupportedOperationException("Not Implemented");

    }

}
