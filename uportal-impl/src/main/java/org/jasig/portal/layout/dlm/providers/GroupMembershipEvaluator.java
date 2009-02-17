/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.dlm.providers;

import javax.persistence.Entity;

import org.dom4j.DocumentHelper;
import org.dom4j.Element;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupConstants;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.layout.dlm.Evaluator;
import org.jasig.portal.layout.dlm.EvaluatorFactory;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.GroupService;


/**
 * Answers isApplicable() in the affirmative if the user represented by the
 * passed in IPerson is a member of the group whose name is passed to the
 * constructor of this class.
 * 
 * Added support for a 'deepMemberOf' mode, from 2.5 patches
 * (UP-1284) and cache group key rather than the group itself 
 * (UP-1532).  d.e 2006/12/19. 
 * 
 * @author mboyd@sungardsct.com
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
@Entity
public class GroupMembershipEvaluator extends Evaluator
{
    private static final int MEMBER_OF_MODE = 0;
    
    private static final int DEEP_MEMBER_OF_MODE = 1;

    private String groupName = null;

    private String groupKey = null;

    private int evaluatorMode;

    /**
     * Zero-arg constructor required by JPA.  Other Java code should not use it.
     */
    public GroupMembershipEvaluator() {}
    
    public GroupMembershipEvaluator(String mode, String name)
    {
        if (mode.equals("memberOf"))
        {
            evaluatorMode = MEMBER_OF_MODE;
        }
        else if (mode.equals("deepMemberOf"))
        {
            evaluatorMode = DEEP_MEMBER_OF_MODE;
        }
        else
        {
            throw new RuntimeException("Unsupported mode '" + mode
                    + "' specified. Only 'memberOf' and 'deepMemberOf' are "
                    + "supported at this time.");
        }
        this.groupName = name;
        this.groupKey = getGroupKey();
    }

    private String getGroupKey()
    {
        EntityIdentifier[] groups = null;
        try
        {
            groups = GroupService.searchForGroups(groupName,
                IGroupConstants.IS, org.jasig.portal.security.IPerson.class);
        } catch (GroupsException e1)
        {
            throw new RuntimeException("An exception occurred searching for " +
                    "the group " + groupName + ".", e1);
        }
        if (groups == null || groups.length == 0)
            throw new RuntimeException("Group with name '" + groupName
                    + "' not found for " + this.getClass().getName()
                    + ". All evaluations will return false.");
        
        return groups[0].getKey();
    }
        
      private IEntityGroup getGroup(String key)
      {
        try
        {
            return GroupService.findGroup(key);
        } catch (GroupsException e)
        {
            throw new RuntimeException("An exception occurred retrieving " +
                    "the group " + groupName + ".", e);
        }
    }

      public boolean isApplicable(IPerson p)
      {
          if (groupKey == null || p == null)
              return false;
          
          IEntityGroup group = getGroup(groupKey);
          EntityIdentifier ei = p.getEntityIdentifier();
          
          try
          {
              IGroupMember groupMember = GroupService.getGroupMember(ei);
              boolean isMember =false;
              
              if (evaluatorMode == MEMBER_OF_MODE)
              {
                  isMember = groupMember.isMemberOf(group);
              }
              else
              { 
                  isMember = groupMember.isDeepMemberOf(group);
              }
              return isMember;
          } catch (GroupsException e)
          {
              throw new RuntimeException("Unable to determine if user "
                      + p.getFullName() + " is in group " + groupName + ".", e);
          }
      }

    @Override
    public void toElement(Element parent) {

        // Assertions.
        if (parent == null) {
            String msg = "Argument 'parent' cannot be null.";
            throw new IllegalArgumentException(msg);
        }
        
        String mde = null;
        switch (this.evaluatorMode) {
            case MEMBER_OF_MODE:
                mde = "memberOf";
                break;
            case DEEP_MEMBER_OF_MODE:
                mde = "deepMemberOf";
                break;
            default:
                throw new IllegalStateException("Unrecognized evaluatorMode constant:  " + this.evaluatorMode);
        }
        
        Element rslt = DocumentHelper.createElement("attribute");
        rslt.addAttribute("mode", mde);
        rslt.addAttribute("name", this.groupName);
        parent.add(rslt);
        
    }
    
    @Override
    public Class<? extends EvaluatorFactory> getFactoryClass() {
        return GroupMembershipEvaluatorFactory.class;
    }

}