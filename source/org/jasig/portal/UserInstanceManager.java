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
 *
 * formatted with JxBeauty (c) johann.langhofer@nextra.at
 */


package  org.jasig.portal;

import  javax.servlet.http.HttpServletRequest;
import  javax.servlet.http.HttpSessionBindingEvent;
import  javax.servlet.http.HttpSessionBindingListener;
import  java.util.HashMap;
import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.security.PersonManager;


/**
 * Caches UserInstance objects
 */
public class UserInstanceManager {
  private static HashMap userInstanceCache = new HashMap(200);
  private static GuestUserInstance m_guestUserInstance = null;

  /**
   * Returns the UserInstance object that is associated with the given request.
   * @param request Incoming HttpServletRequest
   * @return UserInstance object associated with the given request
   */
  public static UserInstance getUserInstance (HttpServletRequest request) {
    // Retrieve the person object that is associated with the request
    IPerson person = PersonManager.getPerson(request);
    // Return the UserInstance from the cache if it exists
    UserInstance userInstance = (UserInstance)userInstanceCache.get(request.getSession(false).getId());
    if (userInstance != null) {
      return  (userInstance);
    }
    // Create either a UserInstance or a GuestUserInstance
    if (person.isGuest() && !person.getSecurityContext().isAuthenticated()) {
      GuestUserInstance guestUserInstance = new GuestUserInstance(person);
      guestUserInstance.registerSession(request);
      userInstance = guestUserInstance;
    } 
    else {
      userInstance = new UserInstance(person);
    }
    // Add the new userInstance to the cache
    userInstanceCache.put(request.getSession().getId(), userInstance);
    // Add the session listener to the session
    request.getSession().setAttribute("UIMSL", new UserInstanceManagerSessionListener());
    // Return the new UserInstance
    return  (userInstance);
  }

  /**
   * Removes UserInstance from the cache
   * @param request
   */
  public static void removeUserInstance (HttpServletRequest request) {
    // Remove the user instance from the cache
    userInstanceCache.remove(request.getSession().getId());
  }

  private static class UserInstanceManagerSessionListener
      implements HttpSessionBindingListener {

    /**
     * put your documentation comment here
     * @param bindingEvent
     */
    public void valueBound (HttpSessionBindingEvent bindingEvent) {}

    /**
     * Removes UserInstance from cache when associated session expires
     * @param bindingEvent
     */
    public void valueUnbound (HttpSessionBindingEvent bindingEvent) {
      // Remove the UserInstance from the cache
      userInstanceCache.remove(bindingEvent.getSession().getId());
    }
  }
}



