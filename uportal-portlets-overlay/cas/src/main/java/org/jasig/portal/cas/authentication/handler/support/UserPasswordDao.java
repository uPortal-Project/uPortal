/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.cas.authentication.handler.support;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface UserPasswordDao {
    /**
     * @param userName Name of the user to get the password has for.
     * @return Password hash for the specified user, null if no hash exists for the user or the user does not exist.
     */
    public String getPasswordHash(String userName);
}
