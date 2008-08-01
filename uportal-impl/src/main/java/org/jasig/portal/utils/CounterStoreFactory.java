/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import org.jasig.portal.PortalException;
import org.jasig.portal.spring.PortalApplicationContextLocator;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.context.ApplicationContext;

/**
 * Produces an implementation of ICounterStore
 * @author <a href="mailto:pkharchenko@unicon.net">Peter Kharchenko</a>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated Use the Spring managed 'counterStore' bean via injection instead
 */
@Deprecated
public class CounterStoreFactory implements DisposableBean {
    private static ICounterStore counterStore;
    
    public static ICounterStore getCounterStoreImpl() throws PortalException {
        if (counterStore == null) {
            final ApplicationContext applicationContext = PortalApplicationContextLocator.getApplicationContext();
            counterStore = (ICounterStore)applicationContext.getBean("counterStore", ICounterStore.class);
        }

        return counterStore;
    }
    

    public void setCounterStore(ICounterStore counterStore) {
        CounterStoreFactory.counterStore = counterStore;
    }

    public void destroy() throws Exception {
        counterStore = null;
    }
}
