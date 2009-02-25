/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
