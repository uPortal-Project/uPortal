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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.jasig.portal.security.IPersonManager;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractPersonManager implements IPersonManager {

    /**
       * Returns a primitive boolean to see if the user is an impersonation. 
       */
    @Override
    public boolean isImpersonating(HttpServletRequest request) {
          final HttpSession session = request.getSession();
          final Boolean sessionVal = (Boolean)session.getAttribute(IS_IMPERSONATING);
          if (sessionVal != null) {
              return sessionVal;
          } 
          
          return true;
      }

    /**
       * sets if the user is actually another user impersonating as this user, to allow obfuscation of private non-technical information
       */
    @Override
    public void setImpersonating(HttpServletRequest request, boolean isImpersonating) {
          final HttpSession session = request.getSession();
          session.setAttribute(IS_IMPERSONATING, isImpersonating);
      }

}