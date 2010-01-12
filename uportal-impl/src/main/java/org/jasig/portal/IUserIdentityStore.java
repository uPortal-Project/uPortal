/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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

  /**
   * Return the username to be used for authorization (exit hook)
   * @param person
   * @return usernmae
   */
  public String getUsername(IPerson person);
  
  /**
   * Gets a portal user name that is associated with the specified portal
   * ID.
   * 
   * @param uPortalUID The portal ID to find a user name for.
   * @return The user name associated with the specified portal id, null if one isn't found.
   * @throws Exception If there are any problems retrieving the user name.
   */
  public String getPortalUserName(int uPortalUID);


}