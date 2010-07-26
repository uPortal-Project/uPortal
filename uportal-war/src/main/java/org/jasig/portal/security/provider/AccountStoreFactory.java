/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.security.provider;



import org.jasig.portal.PortalException;

/**
 * A factory for account store implementation.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision$
 */
public class AccountStoreFactory {
    private static IAccountStore accountStoreImpl = null;

    /**
     * Returns an instance of the {@link RDBMAccountStore}. 
     * Production installations are highly unlikely to use this factory, so the implementation choice is hard-coded in the factory.
     * @return an IAccountStore implementation
     * @exception PortalException if an error occurs
     */
    public static IAccountStore getAccountStoreImpl() throws PortalException {
        if(accountStoreImpl==null) {
            initialize();
        }
        return accountStoreImpl;
    }
    
    private static void initialize() throws PortalException {
        try {
            // Create an instance of the IAccountStore as specified in portal.properties
            accountStoreImpl = new RDBMAccountStore();
        } catch (Exception e) {
            throw new PortalException("AccountStoreFactory: Could obtain an instance of RDBMAccountStore", e);
        }
    }
}
