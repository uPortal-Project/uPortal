/**
 * Copyright � 2001 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal;

import org.jasig.portal.security.IPerson;

/**
 * Interface for managing creation and removal of User Portal Data
 * @author Susan Bramhall
 * @version 1.0
 */
public interface IUserIdentityStore {
  /**
   * Returns a unique uPortal key for a user.
   * @param person the person object
   * @return uPortalUID number
   * @throws Exception exception if an error occurs.
   */
  public int getPortalUID(IPerson person) throws Exception;

  /**
   * Returns a unique uPortal key for a user.  A boolean flag
   * determines whether or not to auto-create data for a new user.
   * @param person object, boolean createPortalData indicating whether to try to create all uPortal data for this user.
   * @return uPortalUID number or -1 if no user found and unable to create user.
   * @throws AuthorizationException if createPortalData is false and no user is found
   *         or if a sql error is encountered
   */
  public int getPortalUID(IPerson person, boolean createPortalData) throws AuthorizationException;

  public void removePortalUID(int uPortalUID) throws Exception;

}