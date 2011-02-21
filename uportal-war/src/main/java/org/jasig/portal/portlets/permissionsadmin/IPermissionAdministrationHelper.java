package org.jasig.portal.portlets.permissionsadmin;

import java.util.Collection;
import java.util.List;

import org.jasig.portal.layout.dlm.remoting.JsonEntityBean;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;
import org.jasig.portal.security.IPerson;

/**
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
public interface IPermissionAdministrationHelper {

    public static final String PERMISSIONS_OWNER = "UP_PERMISSIONS";
    public static final String VIEW_OWNER = "VIEW_OWNER";
    public static final String EDIT_OWNER = "EDIT_OWNER";
    public static final String VIEW_ACTIVITY = "VIEW_ACTIVITY";
    public static final String EDIT_ACTIVITY = "EDIT_ACTIVITY";
    public static final String EDIT_PERMISSION = "EDIT_PERMISSION";
    public static final String VIEW_PERMISSION = "VIEW_PERMISSION";

    public boolean canEditOwner(IPerson currentUser, String owner);

    public boolean canViewOwner(IPerson currentUser, String owner);

    public boolean canEditActivity(IPerson currentUser, String activity);

    public boolean canViewActivity(IPerson currentUser, String activity);

    public boolean canEditPermission(IPerson currentUser, String target);

    public boolean canViewPermission(IPerson currentUser, String target);

    public List<String> getPrincipalsForEntities(Collection<JsonEntityBean> entities);

    public List<JsonEntityBean> getEntitiesForPrincipals(Collection<String> principals);

    public List<String> getCurrentPrincipals(IPermissionOwner owner,
            IPermissionActivity activity, String targetKey);

}