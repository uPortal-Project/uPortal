package org.apereo.portal.security;

import org.apereo.portal.portlet.om.IPortletDefinition;

public interface IPortletPermissionHandler {

    boolean checkPermission(IAuthorizationPrincipal ap, IPortletDefinition portletDefinition);
}
