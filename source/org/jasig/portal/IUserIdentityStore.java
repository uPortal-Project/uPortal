/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import org.jasig.portal.security.IPerson;

/**
 * Interface for managing creation and removal of User Portal Data
 * @author Susan Bramhall
 * @version $Revision$ $Date$
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
   * @param person person whose portalUID will be returned
   * @param createPortalData indicates whether to try to create all uPortal data for a new user.
   * @return uPortalUID number or -1 if no user found and unable to create user.
   * @throws AuthorizationException if createPortalData is false and no user is found
   *         or if a sql error is encountered
   */
  public int getPortalUID(IPerson person, boolean createPortalData) throws AuthorizationException;

  public void removePortalUID(int uPortalUID) throws Exception;

}