/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.providers;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;


/**
 * Answers isApplicable() in the affirmative if the user represented by the
 * passed in IPerson is a member of the group whose name is passed to the
 * constructor of this class.
 * 
 * @author mboyd@sungardsct.com
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public class GroupMembershipEvaluator implements Evaluator
{
    private String groupName = null;

    private IEntityGroup group = null;

    public GroupMembershipEvaluator(String mode, String name) throws Exception
    {
        if (! mode.equals("memberOf"))
        {
            throw new Exception(
                    "Unsupported mode '"
                            + mode
                            + "' specified. Only 'memberOf' is " +
                                    "supported at this time.");
        }
        this.groupName = name;
        this.group = getGroup();
    }

    private IEntityGroup getGroup() throws Exception
    {
        IEntityGroup theGroup = null;
        EntityIdentifier[] groups = GroupService.searchForGroups(groupName,
                IGroupConstants.IS, org.jasig.portal.security.IPerson.class);

        if (groups == null || groups.length == 0)
            throw new Exception("Group with name '" + groupName
                    + "' not found for " + this.getClass().getName()
                    + ". All evaluations will return false.");
        theGroup = GroupService.findGroup(groups[0].getKey());
        if (theGroup == null)
            throw new Exception("Person Group with key '" + groups[0].getKey()
                    + "' not found for " + this.getClass().getName()
                    + ". All evaluations will return false.");
        return theGroup;
    }

    public boolean isApplicable(IPerson p)
    {
        if (group == null || p == null)
            return false;

        try
        {
            EntityIdentifier ei = p.getEntityIdentifier();
            IGroupMember groupMember = GroupService.getGroupMember(ei);
            return groupMember.isMemberOf(group);
        } catch (GroupsException e)
        {
            throw new RuntimeException("Unable to determine if user "
                    + p.getFullName() + " is in group " + groupName + ".", e);
        }
    }
}