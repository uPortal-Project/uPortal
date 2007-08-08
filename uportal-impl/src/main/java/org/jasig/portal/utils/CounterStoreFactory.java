/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import org.jasig.portal.PortalException;
import org.jasig.portal.properties.PropertiesManager;

/**
 * Produces an implementation of ICounterStore
 * @author <a href="mailto:pkharchenko@unicon.net">Peter Kharchenko</a>
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 */
public class CounterStoreFactory {
    private static ICounterStore counterStoreImpl = null;

    /**
     * Returns an instance of the ICounterStore specified in portal.properties
     * @return an ICounterStore implementation
     * @exception PortalException if an error occurs
     */
    public static ICounterStore getCounterStoreImpl() throws PortalException {
        if(counterStoreImpl==null) {
            initialize();
        }
        return counterStoreImpl;
    }
    
    private static void initialize() throws PortalException {
        // Retrieve the class name of the concrete ICounterStore implementation
        String className = PropertiesManager.getProperty("org.jasig.portal.utils.CounterStoreFactory.implementation");
        // Fail if this is not found
        if (className == null)
            throw new PortalException("CounterStoreFactory: org.jasig.portal.utils.CounterStoreFactory.implementation must be specified in portal.properties");
        try {
            // Create an instance of the ICounterStore as specified in portal.properties
            counterStoreImpl = (ICounterStore)Class.forName(className).newInstance();
        } catch (Exception e) {
            throw new PortalException("CounterStoreFactory: Could not instantiate " + className, e);
        }
    }


}



