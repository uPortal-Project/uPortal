/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.container.services;

import org.apache.pluto.core.DefaultOptionalContainerServices;
import org.apache.pluto.spi.optional.PortalAdministrationService;
import org.apache.pluto.spi.optional.PortletEnvironmentService;
import org.apache.pluto.spi.optional.PortletInfoService;
import org.apache.pluto.spi.optional.PortletPreferencesService;
import org.apache.pluto.spi.optional.UserInfoService;
import org.springframework.beans.factory.annotation.Required;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class OptionalContainerServicesImpl extends DefaultOptionalContainerServices {
    private UserInfoService userInfoService;
    
    /**
     * @param userInfoService the userInfoService to set
     */
    @Required
    public void setUserInfoService(UserInfoService userInfoService) {
        this.userInfoService = userInfoService;
    }
    

    /* (non-Javadoc)
     * @see org.apache.pluto.core.DefaultOptionalContainerServices#getPortalAdministrationService()
     */
    @Override
    public PortalAdministrationService getPortalAdministrationService() {
        // TODO return admin listener to allow for session resets & invalidations
        return super.getPortalAdministrationService();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.core.DefaultOptionalContainerServices#getPortletEnvironmentService()
     */
    @Override
    public PortletEnvironmentService getPortletEnvironmentService() {
        // TODO tie in for request attribute callback service
        // TODO RFI for pluto to add request attribute callback service
        return super.getPortletEnvironmentService();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.core.DefaultOptionalContainerServices#getPortletInfoService()
     */
    @Override
    public PortletInfoService getPortletInfoService() {
        // TODO tie into channel manager info for title & such
        return super.getPortletInfoService();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.core.DefaultOptionalContainerServices#getPortletPreferencesService()
     */
    @Override
    public PortletPreferencesService getPortletPreferencesService() {
        // TODO tie into preferences daos, include logic for dupe pref setting
        return super.getPortletPreferencesService();
    }

    /* (non-Javadoc)
     * @see org.apache.pluto.core.DefaultOptionalContainerServices#getUserInfoService()
     */
    @Override
    public UserInfoService getUserInfoService() {
        return this.userInfoService;
    }

}
