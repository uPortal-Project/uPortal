/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

import org.jasig.portal.spring.locator.UserIdentityStoreLocator;

/**
 * Produces an implementation of IUserIdentityStore
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * @deprecated Use {@link IUserIdentityStore} from the Spring applicationContext instead.
 */
@Deprecated
public class UserIdentityStoreFactory {

  /**
   * Returns an instance of the IUserIdentityStore specified in portal.properties
   * @return an IUserIdentityStore implementation
   * @deprecated Use {@link IUserIdentityStore} from the Spring applicationContext instead.
   */
  public static IUserIdentityStore getUserIdentityStoreImpl() {
	  IUserIdentityStore userIdentityStore = UserIdentityStoreLocator.getUserIdentityStore();
	  return userIdentityStore;
  }
}



