/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
