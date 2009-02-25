/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.security.provider;

/**
 * A store for basic account information; username, passwords, etc.
 * Note: this interface is particular to the reference security provider
 * and is not part of the core portal interfaces.
 *
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version $Revision$
 */

public interface IAccountStore {

    /**
     * Obtain account information for a given username
     *
     * @param username a <code>String</code> value
     * @return a <code>String[]</code> array containing (in the order given):
     * md5 password, first name, last name.
     * @exception Exception if an error occurs
     */
    public String[] getUserAccountInformation(String username) throws Exception;
}
