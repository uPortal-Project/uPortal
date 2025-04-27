package org.apereo.portal.security.provider;

import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPortletPermissionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("managePermissionHandler")
public class ManagePermissionHandler implements IPortletPermissionHandler {

    private IAuthorizationService authorizationService;

    @Autowired
    public ManagePermissionHandler(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public boolean checkPermission(
            IAuthorizationPrincipal ap, IPortletDefinition portletDefinition) {
        return authorizationService.canPrincipalManage(
                ap, portletDefinition.getPortletDefinitionId().getStringId());
    }
}
