/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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



