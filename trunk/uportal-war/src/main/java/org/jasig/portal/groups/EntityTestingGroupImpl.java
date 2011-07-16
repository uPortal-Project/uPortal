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
            ? gm.isMemberOf( this )
            : super.contains(gm);
    }
}
