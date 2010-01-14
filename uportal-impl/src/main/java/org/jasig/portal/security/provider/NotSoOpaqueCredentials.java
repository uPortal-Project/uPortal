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

package org.jasig.portal.security.provider;

import org.jasig.portal.security.IOpaqueCredentials;

/**
 * <p>This is a provider-specific extention to the standard opaque credentials
 * that is designed to work with the caching security context. This interface
 * adds a method that removes a bit of the opacity of the original by providing
 * means of retreiving a stored credential. See the warnings and caveats
 * associated with the CacheSecurityContext provider.</p>
 *
 * @author Andrew Newman, newman@yale.edu
 * @version $Revision$
 */


public interface NotSoOpaqueCredentials extends IOpaqueCredentials {

  /**
   * Returns the stored credentials as a String.
   * @return the strored credentials.
   */
  public String getCredentials();
}