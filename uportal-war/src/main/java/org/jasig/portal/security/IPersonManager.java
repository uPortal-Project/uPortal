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

import javax.servlet.http.HttpServletRequest;

/**
 * Manages the storage of an IPerson object in a user's session.
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 */
public interface IPersonManager {

  // Can be used to store an instance of IPerson in the user's session
  public static final String PERSON_SESSION_KEY = "org.jasig.portal.security.IPerson";

  /**
   * Returns an IPerson associated with the incoming request
   * @param request
   * @return IPerson associated with the incoming request
   */
  public IPerson getPerson(HttpServletRequest request) throws PortalSecurityException;
}



