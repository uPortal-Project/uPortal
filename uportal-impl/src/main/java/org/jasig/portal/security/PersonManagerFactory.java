/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;

import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 * @deprecated The 'personManager' bean in the spring context should be used instead
 */
@Deprecated
public class PersonManagerFactory {

    /**
     * Returns an instance of the IPersonManager specified in portal.properties
     * @return instance of the IPersonManager
     */
    public static IPersonManager getPersonManagerInstance() {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final IPersonManager personManager = (IPersonManager) applicationContext.getBean("personManager", IPersonManager.class);
        return personManager;
    }
}
