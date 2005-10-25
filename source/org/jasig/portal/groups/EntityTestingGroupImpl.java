/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups;


/**
 * @author Dan Ellentuck
 * @version $Revision$
 * @see org.jasig.portal.groups.IEntityGroup
 * 
 * An <code>IEntityGroup</code> that answers if it contains an entity 
 * by delegating to the local group store.  By contrast, an 
 * <code>EntityGroupImpl</code> answers this by examining (and if 
 * necessary initializing) its own member cache.  This behavior is 
 * designed to accommodate groups whose membership is computed by 
 * testing the prospective entity member rather than by testing the 
 * group.  It allows contains() and deepContains() to work correctly 
 * for groups from services like PAGS and JitLDAP.  Groups in these
 * services do not keep references to their members but only define 
 * the logic for computing if a candidate entity is a member.
 */
public class EntityTestingGroupImpl extends EntityGroupImpl {

    /**
     * @param groupKey
     * @param entityType
     * @throws GroupsException
     */
    public EntityTestingGroupImpl(String groupKey, Class entityType) throws GroupsException {
        super(groupKey, entityType);
    }
    /**
     * Checks if <code>GroupMember</code> gm is a member of this.
     * @return boolean
     * @param gm org.jasig.portal.groups.IGroupMember
     */
    public boolean contains(IGroupMember gm) throws GroupsException
    {
        return ( gm.isEntity() )
            ? getLocalGroupService().contains(this,gm)
            : super.contains(gm);
    }
}
