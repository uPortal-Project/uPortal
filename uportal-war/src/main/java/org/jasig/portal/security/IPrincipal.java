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