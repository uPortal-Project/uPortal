/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.security;

/**
 * <p>An interface that defines the required methods for assigning and
 * retrieving information about the authenticated principal (user). Providers
 * requiring additional principal information should extend this interface
 * rather than replacing it.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */

public interface IPrincipal extends java.io.Serializable {

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