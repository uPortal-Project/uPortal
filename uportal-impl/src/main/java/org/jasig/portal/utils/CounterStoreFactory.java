/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.utils;

import org.jasig.portal.PortalException;
import org.jasig.portal.spring.locator.CounterStoreLocator;

/**
 * Produces an implementation of ICounterStore
 * @author <a href="mailto:pkharchenko@unicon.net">Peter Kharchenko</a>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated Use the Spring managed 'counterStore' bean via injection instead
 */
@Deprecated
public class CounterStoreFactory {
    private static ICounterStore counterStore;
    
    public static ICounterStore getCounterStoreImpl() throws PortalException {
        return CounterStoreLocator.getCounterStore();
    }
}
