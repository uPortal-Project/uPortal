/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security.provider;

/**
 * A store for basic account information; username, passwords, etc.
 * Note: this interface is particular to the reference security provider
 * and is not part of the core portal interfaces.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
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
