package org.apereo.portal.security.provider;

import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPortletPermissionHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service("configurePermissionHandler")
public class ConfigurePermissionHandler implements IPortletPermissionHandler {

    private IAuthorizationService authorizationService;

    @Autowired
    public ConfigurePermissionHandler(IAuthorizationService authorizationService) {
        this.authorizationService = authorizationService;
    }

    @Override
    public boolean checkPermission(
            IAuthorizationPrincipal ap, IPortletDefinition portletDefinition) {
        return authorizationService.canPrincipalConfigure(
                ap, portletDefinition.getPortletDefinitionId().getStringId());
    }
}
