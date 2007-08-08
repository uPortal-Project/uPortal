package org.jasig.portal.security;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.groups.GroupsException;
import org.jasig.portal.groups.IEntityGroup;
import org.jasig.portal.groups.IGroupMember;
import org.jasig.portal.services.AuthorizationService;
import org.jasig.portal.services.GroupService;

/**
 * Provides single location for housing knowledge of the various ways to
 * determine if a user is an administrator or is in any administrative
 * sub-group.
 * 
 * @author Mark Boyd
 */
public class AdminEvaluator
{
    private static final Log cLog = LogFactory.getLog(AdminEvaluator.class);
    
    /**
     * Determines if the passed-in IPerson represents a user that is a member
     * of the administrator group or any of its sub groups.
     *
     * @param p
     * @return
     */
    public static boolean isAdmin(IPerson p)
    {
        IAuthorizationPrincipal iap = AuthorizationService.instance()
                .newPrincipal(p.getEntityIdentifier().getKey(),
                        p.getEntityIdentifier().getType());
        
        return isAdmin(iap);
    }

    /**
     * Determines if the passed-in authorization principal represents a user
     * that is a member of the administrator group or any of its sub groups.
     * 
     * @param p
     * @return
     */
    public static boolean isAdmin(IAuthorizationPrincipal ap)
    {
        IGroupMember member = AuthorizationService.instance().getGroupMember(ap);
        return isAdmin(member);
    }

    /**
     * Determines if the passed-in group member represents a user
     * that is a member of the administrator group or any of its sub groups.
     * 
     * @param p
     * @return
     */
    public static boolean isAdmin(IGroupMember member)
    {
        IEntityGroup adminGroup = null;

        try
        {
            adminGroup = GroupService
                    .getDistinguishedGroup(GroupService.PORTAL_ADMINISTRATORS);
        } catch (GroupsException ge)
        {
            // cannot determine whether or not the user is an admin.
            cLog.error("Administrative group not found, cannot determine " +
                    "user's admininstrative membership.", ge);
        }

        return (null != adminGroup && adminGroup.deepContains(member));
    }
}
