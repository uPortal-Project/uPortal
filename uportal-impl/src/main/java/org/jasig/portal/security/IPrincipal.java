/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.security;

import java.io.Serializable;

/**
 * <p>An interface that defines the required methods for assigning and
 * retrieving information about the authenticated principal (user). Providers
 * requiring additional principal information should extend this interface
 * rather than replacing it.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */
public interface IPrincipal extends Serializable {

  /**
   * Returns the locally unique username or user identifier for this
   * principal.
   */
  public String getUID();

  /**
   * Returns the globally unique user identifier for this principal. This
   * identifier should be maximally unique within the scope of the deployed
   * security mechanism.
   *
   */
  public String getGlobalUID();

  /**
   * Returns the human-readable name of the principal. This should be either
   * their first and last name or whatever local convention dicates should be
   * returned by the CommonName (CN) attribute for those security contexts
   * using X.509 style naming.
   */
  public String getFullName();

  /**
   * Sets the locally unique username in preparation for authentication. Note
   * that post-authentication, an attempt to set a UID may either fail or
   * reset the authentication status of the security context container.
   *
   * @param UID The desired locally unique UID value.
   */
  public void setUID(String UID);
}