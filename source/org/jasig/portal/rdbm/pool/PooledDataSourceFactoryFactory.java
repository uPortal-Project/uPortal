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
package org.jasig.portal.rdbm.pool;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserLayoutStoreFactory;
import org.jasig.portal.properties.PropertiesManager;

/**
 * @author Eric Dalquist <a href="mailto:edalquist@unicon.net">edalquist@unicon.net</a>
 * @version $Revision $
 */
public final class PooledDataSourceFactoryFactory {

    private static final Log log = LogFactory.getLog(PooledDataSourceFactoryFactory.class);

    private static final String DEFAULT_CLASS_NAME = "org.jasig.portal.RDBMPortletPreferencesStore";

    private static IPooledDataSourceFactory pooledDataSourceFactoryImpl = null;
    private static String className = null;

    static {
        try {
            // Retrieve the class name of the concrete IPortletPreferencesStore implementation
            className = PropertiesManager.getProperty("org.jasig.portal.PooledDataSourceFactory.implementation");
        }
        catch (Exception e) { }

        if (className == null || className.length() == 0)
            log.error("org.jasig.portal.PooledDataSourceFactory.implementation must be specified in portal.properties");
    }

    public static final IPooledDataSourceFactory getPooledDataSourceFactory() {
        try {
            return getPooledDataSourceFactory(className);
        }
        catch (PortalException pe) {
            log.error(" Could not load " + className, pe);

            try {
                return getPooledDataSourceFactory(DEFAULT_CLASS_NAME);
            }
            catch (PortalException pe1) {
                log.error("Could not load " + DEFAULT_CLASS_NAME, pe1);
                return null;
            }
        }
    }

    protected static IPooledDataSourceFactory getPooledDataSourceFactory(String className)
            throws PortalException {
        try {
            if (pooledDataSourceFactoryImpl == null) {
                synchronized (UserLayoutStoreFactory.class) {
                    if (pooledDataSourceFactoryImpl == null) {
                        pooledDataSourceFactoryImpl = (IPooledDataSourceFactory)Class.forName(className).newInstance();
                    }
                }
            }
            
            return pooledDataSourceFactoryImpl;
        }
        catch (Exception e) {
            log.error("Could not instantiate " + className, e);
            throw new PortalException(e.getMessage());
        }
    }

    private PooledDataSourceFactoryFactory() { }
}