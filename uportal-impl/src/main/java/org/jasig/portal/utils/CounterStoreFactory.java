/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import org.jasig.portal.PortalException;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.context.ApplicationContext;

/**
 * Produces an implementation of ICounterStore
 * @author <a href="mailto:pkharchenko@unicon.net">Peter Kharchenko</a>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated Use the Spring managed 'counterStore' bean via injection instead
 */
@Deprecated
public class CounterStoreFactory {
    public static ICounterStore getCounterStoreImpl() throws PortalException {
        final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
        final ICounterStore counterStore = (ICounterStore)applicationContext.getBean("counterStore", ICounterStore.class);
        return counterStore;
    }
}
