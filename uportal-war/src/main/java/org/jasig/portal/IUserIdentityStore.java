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
  public int getPortalUID(IPerson person) throws AuthorizationException;

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

  public void removePortalUID(String userName) throws Exception;
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
  
  /**
   * Gets a portal user id that is associated with the specified portal user name
   */
  public Integer getPortalUserId(String userName);
  
  /**
   * @return true if the specified username is used as a default user for any other user in the portal
   */
  public boolean isDefaultUser(String username);

}