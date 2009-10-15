/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

import java.util.Arrays;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jasig.portal.channel.IChannelDefinition;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntity;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.groups.ILockableEntityGroup;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;

/**
 * <p>Abstract channel registry store.  Be aware that calls to methods within
 * this class may be cached.  The cache is configured in
 * /uportal-impl/src/main/resources/properties/contexts/channelContainerContext.xml</p>
 */
public abstract class AbstractChannelRegistryStore implements IChannelRegistryStore {
    
    /**
     * Makes one category a child of another.
     * @param child the source category
     * @param parent the destination category
     * @throws org.jasig.portal.groups.GroupsException
     */
    public void addCategoryToCategory(ChannelCategory child, ChannelCategory parent) throws GroupsException {
        String childKey = String.valueOf(child.getId());
        IEntityGroup childGroup = GroupService.findGroup(childKey);
        String parentKey = String.valueOf(parent.getId());
        IEntityGroup parentGroup = GroupService.findGroup(parentKey);
        parentGroup.addMember(childGroup);
        parentGroup.updateMembers();
    }

    /**
     * Associates a channel definition with a category.
     * @param channelDef the channel definition
     * @param category the channel category to which to associate the channel definition
     * @throws org.jasig.portal.PortalException
     */
    public void addChannelToCategory(IChannelDefinition channelDef, ChannelCategory category) throws PortalException {
        String channelDefKey = String.valueOf(channelDef.getId());
        IEntity channelDefEntity = GroupService.getEntity(channelDefKey, IChannelDefinition.class);
        IEntityGroup categoryGroup = GroupService.findGroup(category.getId());
        categoryGroup.addMember(channelDefEntity);
        categoryGroup.updateMembers();
    }

    /**
     * Sets a channel definition as "approved".  This effectively makes a
     * channel definition available in the channel registry, making the channel
     * available for subscription to those authorized to subscribe to it.
     * This method is a convenience method. As an alternative to calling
     * this method, one could simply set the approver ID and approval date
     * and then call saveChannelDefinition(ChannelDefinition chanDef).
     * @param channelDef the channel definition to approve
     * @param approver the user that approves this channel definition
     * @param approveDate the date when the channel definition should be approved (can be future dated)
     * @throws Exception
     */
    public void approveChannelDefinition(IChannelDefinition channelDef, IPerson approver, Date approveDate) {
        channelDef.setApproverId(approver.getID());
        channelDef.setApprovalDate(approveDate);
        saveChannelDefinition(channelDef);
    }

    /**
     * Deletes a channel category.
     * @param category the channel category to delete
     * @throws org.jasig.portal.groups.GroupsException
     */
    public void deleteChannelCategory(ChannelCategory category) throws GroupsException {
        String key = String.valueOf(category.getId());
        ILockableEntityGroup categoryGroup = GroupService.findLockableGroup(key,"UP_FRAMEWORK");
        categoryGroup.delete();
    }

    /**
     * Removes a channel from the channel registry by changing
     * its status from "approved" to "unapproved".  Afterwards, no one
     * will be able to subscribe to or render the channel.
     * This method is a convenience method. As an alternative to calling
     * this method, one could simply set the approver ID and approval date
     * to NULL and then call saveChannelDefinition(ChannelDefinition chanDef).
     * @param channelDef the channel definition to disapprove
     * @throws Exception
     */
    public void disapproveChannelDefinition(IChannelDefinition channelDef) {
        channelDef.setApproverId(-1);
        channelDef.setApprovalDate(null);
        saveChannelDefinition(channelDef);
    }

    /**
     * Gets all child channel categories for a parent category.
     * @return channelCategories the children categories
     * @throws org.jasig.portal.groups.GroupsException
     */
    public ChannelCategory[] getAllChildCategories(ChannelCategory parent) {
        Set<ChannelCategory> rslt = new HashSet<ChannelCategory>();

        for (ChannelCategory child : getChildCategories(parent)) {
            // recurse
            rslt.add(child);
            rslt.addAll(Arrays.asList(getAllChildCategories(child)));
        }

        return rslt.toArray(new ChannelCategory[0]);
    }

    /**
     * Gets all child channel definitions for a parent category.
     * @return channelDefinitions the children channel definitions
     * @throws java.sql.SQLException
     * @throws org.jasig.portal.groups.GroupsException
     */
    public IChannelDefinition[] getAllChildChannels(ChannelCategory parent) {
        
        Set<IChannelDefinition> rslt = new HashSet<IChannelDefinition>();
        
        try {
            for (IChannelDefinition channel : getChildChannels(parent)) {
                rslt.add(channel);
            }
            for (ChannelCategory category : getAllChildCategories(parent)) {
                // append channels to list for each child category in the tree
                for (IChannelDefinition channel : getChildChannels(category)) {
                    rslt.add(channel);
                }
            }
            
        } catch (Exception e) {
            String msg = "Failed to obtain child channels for the specified parent '" 
                                    + parent.getName() + "', id=" +parent.getId();
            throw new PortalException(msg, e);
        }

        return (IChannelDefinition[]) rslt.toArray(new IChannelDefinition[0]);

    }

	public IChannelDefinition[] getAllChildChannels(ChannelCategory parent,
			IPerson person) {
        
        Set<IChannelDefinition> rslt = new HashSet<IChannelDefinition>();
        
        try {
            for (IChannelDefinition channel : getChildChannels(parent, person)) {
                rslt.add(channel);
            }
            for (ChannelCategory category : getAllChildCategories(parent)) {
                // append channels to list for each child category in the tree
                for (IChannelDefinition channel : getChildChannels(category, person)) {
                    rslt.add(channel);
                }
            }
            
        } catch (Exception e) {
            String msg = "Failed to obtain child channels for the specified parent '" 
                                    + parent.getName() + "', id=" +parent.getId();
            throw new PortalException(msg, e);
        }

        return (IChannelDefinition[]) rslt.toArray(new IChannelDefinition[0]);
	}

	public IChannelDefinition[] getAllManageableChildChannels(ChannelCategory parent,
			IPerson person) {
        
        Set<IChannelDefinition> rslt = new HashSet<IChannelDefinition>();
        
        try {
            for (IChannelDefinition channel : getManageableChildChannels(parent, person)) {
                rslt.add(channel);
            }
            for (ChannelCategory category : getAllChildCategories(parent)) {
                // append channels to list for each child category in the tree
                for (IChannelDefinition channel : getManageableChildChannels(category, person)) {
                    rslt.add(channel);
                }
            }
            
        } catch (Exception e) {
            String msg = "Failed to obtain child channels for the specified parent '" 
                                    + parent.getName() + "', id=" +parent.getId();
            throw new PortalException(msg, e);
        }

        return (IChannelDefinition[]) rslt.toArray(new IChannelDefinition[0]);
	}

    /**
     * Gets an existing channel category.
     * @param channelCategoryId the id of the category to get
     * @return channelCategory the channel category
     * @throws org.jasig.portal.groups.GroupsException
     */
    public ChannelCategory getChannelCategory(String channelCategoryId) throws GroupsException {
        IEntityGroup categoryGroup = GroupService.findGroup(channelCategoryId);
        ChannelCategory category = new ChannelCategory(channelCategoryId);
        category.setName(categoryGroup.getName());
        category.setDescription(categoryGroup.getDescription());
        category.setCreatorId(categoryGroup.getCreatorID());
        return category;
    }

    /**
     * Gets all child channel categories for a parent category.
     * @return channelCategories the children categories
     * @throws org.jasig.portal.groups.GroupsException
     */
    @SuppressWarnings("unchecked")
    public ChannelCategory[] getChildCategories(ChannelCategory parent) throws GroupsException {
        String parentKey = String.valueOf(parent.getId());
        IEntityGroup parentGroup = GroupService.findGroup(parentKey);
        Set<ChannelCategory> categories = new HashSet<ChannelCategory>();
        Iterator<IGroupMember> iter = parentGroup.getMembers();
        while (iter.hasNext()) {
            IGroupMember gm = iter.next();
            if (gm.isGroup()) {
                String categoryId = gm.getKey();
                categories.add(getChannelCategory(categoryId));
            }
        }
        return (ChannelCategory[]) categories.toArray(new ChannelCategory[0]);
    }

    /**
     * Gets all child channel definitions for a parent category.
     * @return channelDefinitions the children channel definitions
     * @throws java.sql.SQLException
     * @throws org.jasig.portal.groups.GroupsException
     */
    @SuppressWarnings("unchecked")
    public IChannelDefinition[] getChildChannels(ChannelCategory parent) {
        String parentKey = String.valueOf(parent.getId());
        IEntityGroup parentGroup = GroupService.findGroup(parentKey);
        Set<IChannelDefinition> channelDefs = new HashSet<IChannelDefinition>();
        Iterator<IGroupMember> iter = parentGroup.getMembers();
        while (iter.hasNext()) {
            IGroupMember gm = (IGroupMember)iter.next();
            if (gm.isEntity()) {
                int channelPublishId = Integer.parseInt(gm.getKey());
                channelDefs.add(getChannelDefinition(channelPublishId));
            }
        }
        return (IChannelDefinition[])channelDefs.toArray(new IChannelDefinition[channelDefs.size()]);
    }

    /**
     * Gets all child channel definitions for a parent category that the given
     * user is allowed to subscribe to.
     * @return channelDefinitions the children channel definitions for the
     * given person
     */
	public IChannelDefinition[] getChildChannels(ChannelCategory parent,
			IPerson person) {

		EntityIdentifier ei = person.getEntityIdentifier();
	    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
	    
        String parentKey = String.valueOf(parent.getId());
        IEntityGroup parentGroup = GroupService.findGroup(parentKey);
        Set<IChannelDefinition> channelDefs = new HashSet<IChannelDefinition>();
        Iterator<IGroupMember> iter = parentGroup.getMembers();
        while (iter.hasNext()) {
            IGroupMember gm = (IGroupMember)iter.next();
            if (gm.isEntity()) {
                int channelPublishId = Integer.parseInt(gm.getKey());
            	if(ap.canSubscribe(channelPublishId)) {
            		channelDefs.add(getChannelDefinition(channelPublishId));
            	}
            }
        }
        return (IChannelDefinition[])channelDefs.toArray(new IChannelDefinition[channelDefs.size()]);
	}
	
	public IChannelDefinition[] getManageableChildChannels(ChannelCategory parent,
			IPerson person) {

		EntityIdentifier ei = person.getEntityIdentifier();
	    IAuthorizationPrincipal ap = AuthorizationService.instance().newPrincipal(ei.getKey(), ei.getType());
	    
        String parentKey = String.valueOf(parent.getId());
        IEntityGroup parentGroup = GroupService.findGroup(parentKey);
        Set<IChannelDefinition> channelDefs = new HashSet<IChannelDefinition>();
        Iterator<IGroupMember> iter = parentGroup.getMembers();
        while (iter.hasNext()) {
            IGroupMember gm = (IGroupMember)iter.next();
            if (gm.isEntity()) {
                int channelPublishId = Integer.parseInt(gm.getKey());
            	if(ap.canManage(channelPublishId)) {
            		channelDefs.add(getChannelDefinition(channelPublishId));
            	}
            }
        }
        return (IChannelDefinition[])channelDefs.toArray(new IChannelDefinition[channelDefs.size()]);
	}

    /**
     * Gets the immediate parent categories of this category.
     * @return parents, the parent categories.
     * @throws org.jasig.portal.groups.GroupsException
     */
    @SuppressWarnings("unchecked")
    public ChannelCategory[] getParentCategories(ChannelCategory child) throws GroupsException {
        String childKey = String.valueOf(child.getId());
        IEntityGroup childGroup = GroupService.findGroup(childKey);
        Set<ChannelCategory> parents = new HashSet<ChannelCategory>();
        Iterator iter = childGroup.getContainingGroups();
        while (iter.hasNext()) {
            IGroupMember gm = (IGroupMember)iter.next();
            if (gm.isGroup()) {
                String categoryId = gm.getKey();
                parents.add(getChannelCategory(categoryId));
            }
        }
        return (ChannelCategory[]) parents.toArray(new ChannelCategory[0]);
    }

    /**
     * Gets the immediate parent categories of this channel definition.
     * @return parents, the parent categories.
     * @throws org.jasig.portal.groups.GroupsException
     */
    @SuppressWarnings("unchecked")
    public ChannelCategory[] getParentCategories(IChannelDefinition child) throws GroupsException {
        String childKey = String.valueOf(child.getId());
        IEntity childEntity = GroupService.getEntity(childKey, IChannelDefinition.class);
        Set<ChannelCategory> parents = new HashSet<ChannelCategory>();
        Iterator iter = childEntity.getContainingGroups();
        while (iter.hasNext()) {
            IGroupMember gm = (IGroupMember)iter.next();
            if (gm.isGroup()) {
                String categoryId = gm.getKey();
                parents.add(getChannelCategory(categoryId));
            }
        }
        return (ChannelCategory[]) parents.toArray(new ChannelCategory[0]);
    }

    /**
     * Gets top level channel category
     * @return channelCategories the new channel category
     * @throws org.jasig.portal.groups.GroupsException
     */
    public ChannelCategory getTopLevelChannelCategory() throws GroupsException {
        IEntityGroup categoryGroup = GroupService.getDistinguishedGroup(IGroupConstants.CHANNEL_CATEGORIES);
        return getChannelCategory(categoryGroup.getKey());
    }

    /**
     * Creates a new channel category.
     * @return channelCategory the new channel category
     * @throws org.jasig.portal.groups.GroupsException
     */
    public ChannelCategory newChannelCategory() throws GroupsException {
        IEntityGroup categoryGroup = GroupService.newGroup(IChannelDefinition.class);
        categoryGroup.setName(""); // name cannot be null
        categoryGroup.setCreatorID(""); // creatorId cannot be null
        categoryGroup.update();
        String id = categoryGroup.getKey();
        return new ChannelCategory(id);
    }

    /**
     * Creates a new channel category with the specified values.
     * @param name the name of the category
     * @param description the name of the description
     * @param creatorId the id of the creator or system
     * @return channelCategory the new channel category
     * @throws GroupsException
     */
    public ChannelCategory newChannelCategory( String name,
            String description,
            String creatorId )
    throws GroupsException
    {
        IEntityGroup categoryGroup = GroupService.newGroup(IChannelDefinition.class);
        categoryGroup.setName( name ); // name cannot be null
        categoryGroup.setCreatorID( creatorId ); // creatorId cannot be null
        categoryGroup.setDescription( description );
        categoryGroup.update();
        String id = categoryGroup.getKey();
        ChannelCategory cat = new ChannelCategory(id);
        cat.setName( name );
        cat.setDescription( description );
        cat.setCreatorId( creatorId );
        return cat;
    }

    /**
     * Makes one category a child of another.
     * @param child the category to remove
     * @param parent the category to remove from
     * @throws org.jasig.portal.groups.GroupsException
     */
    public void removeCategoryFromCategory(ChannelCategory child, ChannelCategory parent) throws GroupsException {
        String childKey = String.valueOf(child.getId());
        IEntityGroup childGroup = GroupService.findGroup(childKey);
        String parentKey = String.valueOf(parent.getId());
        IEntityGroup parentGroup = GroupService.findGroup(parentKey);
        parentGroup.removeMember(childGroup);
        parentGroup.updateMembers();
    }

    /**
     * Disassociates a channel definition from a category.
     * @param channelDef the channel definition
     * @param category the channel category from which to disassociate the channel definition
     * @throws org.jasig.portal.PortalException
     */
    public void removeChannelFromCategory(IChannelDefinition channelDef, ChannelCategory category) throws PortalException {
        String channelDefKey = String.valueOf(channelDef.getId());
        IEntity channelDefEntity = GroupService.getEntity(channelDefKey, IChannelDefinition.class);
        String categoryKey = String.valueOf(category.getId());
        IEntityGroup categoryGroup = GroupService.findGroup(categoryKey);
        categoryGroup.removeMember(channelDefEntity);
        categoryGroup.updateMembers();
    }

    /**
     * Persists a channel category.
     * @param category the channel category to persist
     * @throws org.jasig.portal.groups.GroupsException
     */
    public void saveChannelCategory(ChannelCategory category) throws GroupsException {
        IEntityGroup categoryGroup = GroupService.findGroup(category.getId());
        categoryGroup.setName(category.getName());
        categoryGroup.setDescription(category.getDescription());
        categoryGroup.setCreatorID(category.getCreatorId());
        categoryGroup.update();
    }

}
