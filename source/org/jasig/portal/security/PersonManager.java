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


package  org.jasig.portal.security;

import  java.util.HashMap;
import  javax.servlet.http.HttpServletRequest;
import  javax.servlet.http.HttpSessionBindingListener;
import  javax.servlet.http.HttpSessionBindingEvent;
import  org.jasig.portal.security.provider.PersonImpl;
import org.jasig.portal.services.LogService;


/**
 * put your documentation comment here
 */
public class PersonManager {
  private static HashMap m_personCache = new HashMap(200);

  /**
   * Retrieve an IPerson object for the incoming request
   * @param request
   * @return 
   */
  public static IPerson getPerson (HttpServletRequest request) {
    // Return the person object if it exists in the cache
    IPerson person = (IPerson)m_personCache.get(request.getSession(false).getId());
    if (person != null) {
      return  (person);
    }
    // Create a new instance of a person
    person = new PersonImpl();
    try {
      // Add the initial security context to the person
      person.setSecurityContext(new InitialSecurityContext("root"));
    } catch (Exception e) {
      // Log the exception
      LogService.log(LogService.ERROR, e);
    }
    // By default new user's have the UID of 1
    person.setID(1);
    // Add a listener to the session
    request.setAttribute("PMSL", new PersonManagerSessionListener());
    // Add this person to the cache
    m_personCache.put(request.getSession(false).getId(), person);
    // Return the new person object
    return  (person);
  }

  /**
   * Removes an IPerson from the cache
   * @param request
   */
  public static void removePerson (HttpServletRequest request) {
    // Remove the user instance from the cache
    m_personCache.remove(request.getSession(false).getId());
  }

  private static class PersonManagerSessionListener
      implements HttpSessionBindingListener {

    /**
     * Called when IPerson is bound into user session
     * @param bindingEvent
     */
    public void valueBound (HttpSessionBindingEvent bindingEvent) {}

    /**
     * Called when IPerson is being unbound from user session
     * @param bindingEvent
     */
    public void valueUnbound (HttpSessionBindingEvent bindingEvent) {
      // DEBUG
      //System.out.println("Removing IPerson " + ((IPerson)m_personCache.get(bindingEvent.getSession().getId())).getID() + 
      //    " for session " + bindingEvent.getSession().getId());
      // Remove the UserInstance from the cache
      m_personCache.remove(bindingEvent.getSession().getId());
    }
  }
}



