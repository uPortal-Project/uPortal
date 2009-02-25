/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.springframework.context.ApplicationContext;

/**
 * @deprecated Use {@link org.jasig.portal.user.IUserInstanceManager} from the Spring Application Context instead.
 */
public class UserInstanceManager {

    /**
     * @deprecated Use {@link org.jasig.portal.user.IUserInstanceManager#getUserInstance(HttpServletRequest)} instead.
     */
    public static IUserInstance getUserInstance(HttpServletRequest request) throws PortalException {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final IUserInstanceManager userInstanceManager = (IUserInstanceManager) applicationContext.getBean("userInstanceManager", IUserInstanceManager.class);
        return userInstanceManager.getUserInstance(request);
    }
}
