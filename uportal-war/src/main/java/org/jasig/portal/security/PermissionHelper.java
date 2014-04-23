package org.jasig.portal.security;

import org.apache.commons.lang3.Validate;
import org.jasig.portal.portlet.om.IPortletDefinition;

/**
 * Stateless static utility convenience methods for working with uPortal permissions.
 * @since uPortal 4.1
 */
public class PermissionHelper {

    /**
     * Static utility method computing the permission target ID for a portlet definition.
     * @param portletDefinition a portlet definition
     * @return String permission target ID for the portlet definition.
     * @throws IllegalArgumentException if portletDefinition is null
     * @since uPortal 4.1
     */
    public static String permissionTargetIdForPortletDefinition(final IPortletDefinition portletDefinition) {

        Validate.notNull(portletDefinition, "Cannot compute permission target ID for a null portlet definition.");

        final String portletPublicationId = portletDefinition.getPortletDefinitionId().getStringId();

        return IPermission.PORTLET_PREFIX.concat(portletPublicationId);
    }
}
