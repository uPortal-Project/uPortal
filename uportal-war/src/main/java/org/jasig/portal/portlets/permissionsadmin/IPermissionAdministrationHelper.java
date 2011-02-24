package org.jasig.portal.portlets.permissionsadmin;

import java.util.Collection;
import java.util.Set;

import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.security.IPerson;

/**
 * IPermissionAdministrationHelper is designed to offer access to common
 * permissions administration operations.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public interface IPermissionAdministrationHelper {

    public static final String PERMISSIONS_OWNER = "UP_PERMISSIONS";
    public static final String EDIT_PERMISSION = "EDIT_PERMISSIONS";
    public static final String VIEW_PERMISSION = "VIEW_PERMISSIONS";
    public static final String ALL_PERMISSIONS_TARGET = "ALL";

    public boolean canEditOwner(IPerson currentUser, String owner);

    public boolean canViewOwner(IPerson currentUser, String owner);

    public boolean canEditActivity(IPerson currentUser, String activity);

    public boolean canViewActivity(IPerson currentUser, String activity);

    public boolean canEditPermission(IPerson currentUser, String target);

    public boolean canViewPermission(IPerson currentUser, String target);

    public Set<String> getPrincipalsForEntities(Collection<JsonEntityBean> entities);

    public Set<JsonEntityBean> getEntitiesForPrincipals(Collection<String> principals);

    public Set<String> getCurrentPrincipals(IPermissionOwner owner,
            IPermissionActivity activity, String targetKey);

}