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


package  org.jasig.portal.jndi;

import  java.util.Hashtable;
import  java.util.Enumeration;
import  javax.naming.Context;
import  javax.naming.InitialContext;
import  javax.naming.NamingException;
import  javax.naming.CompositeName;
import  javax.servlet.http.HttpSession;
import  javax.servlet.http.HttpSessionBindingListener;
import  javax.servlet.http.HttpSessionBindingEvent;
import  org.jasig.portal.InternalPortalException;
import  org.jasig.portal.security.IPerson;
import  org.jasig.portal.services.LogService;
import  org.w3c.dom.Document;
import  org.w3c.dom.NodeList;
import  org.w3c.dom.Node;


/**
 * JNDIManager
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 */
public class JNDIManager {

  /**
   * put your documentation comment here
   */
  public JNDIManager () {
  }

  /**
   * put your documentation comment here
   */
  public static void initializePortalContext () {
    try {
      Context context = getContext();
      // Create a subcontext for portal-wide services
      context.createSubcontext("services");
      // Bind in the logger service
      LogService logger = LogService.instance();
      context.bind("/services/logger", logger);
      // Create a subcontext for user specific bindings
      context.createSubcontext("users");
      // Create a subcontext to keep track of active sessions
      context.createSubcontext("sessions");
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  /**
   * Create a new subcontext for the given sessionID under /sessions
   * @param sessionID
   */
  public static void initializeSessionContext (HttpSession session) throws InternalPortalException {
    // Get the ID of the session
    String sessionID = session.getId();
    Context sessionsContext = null;
    try {
      // Retrieve the subcontext for user sessions
      sessionsContext = (Context)getContext().lookup("/sessions");
    } catch (NamingException ne) {
      LogService.instance().log(LogService.ERROR, "JNDIManager.initializeSessionContext(): Could not find /sessions context - "
          + ne.getMessage());
      throw  new InternalPortalException(ne);
    }
    /*
    Context userSessionContext = null;
        try {
      // Create a subcontext for the new session
      userSessionContext = sessionsContext.createSubcontext(sessionID);
    } catch (NamingException ne) {
      LogService.instance().log(LogService.ERROR, "JNDIManager.initializeSessionContext(): Could not create /sessions/ "
          + sessionID + " context - " + ne.getMessage());
      throw  new InternalPortalException(ne);
      }*/
    // Create a guest IPerson object, this should be retrieved from a factory
    IPerson person = new org.jasig.portal.security.provider.PersonImpl();
    person.setID(1);
    person.setFullName("Guest");
    try {
      // Bind a guest IPerson object to this new subcontext
        sessionsContext.bind(session.getId(), person);
    } catch (NamingException ne) {
      LogService.instance().log(LogService.ERROR, "JNDIManager.initializeSessionContext(): Could not bind IPerson to /sessions/ "
          + sessionID + " context - " + ne.getMessage());
      throw  new InternalPortalException(ne);
    }
  }

  /**
   * put your documentation comment here
   * @param userLayout
   * @param sessionID
   * @param person
   * @exception PortalNamingException
   */
  public static void initializeUserContext (Document userLayout, HttpSession session, IPerson person) throws PortalNamingException {
    try {
      // Bind our session listener to the session
      session.setAttribute("JNDISessionListener", new JNDISessionListener());
      // Throw an exception if the person object is not found
      if (person == null) {
        throw  new PortalNamingException("JNDIManager.initializeUserContext() - Cannot find person object!");
      }
      // Throw an exception if the user's layout cannot be found
      if (userLayout == null) {
        throw  new PortalNamingException("JNDIManager.initializeUserContext() - Cannot find user's layout!");
      }
      // Get the portal wide context
      Context context = getContext();
      // Get the context that keeps track of session IDs
      Context sessionsContext = (Context)context.lookup("sessions");
      // Bind the Person object to it's session ID in JNDI
      sessionsContext.rebind(session.getId(), person);
      // Get the users subcontext
      Context usersContext = (Context)context.lookup("users");
      // Create a subcontext for this specific useer
      Context thisUsersContext = (Context)usersContext.createSubcontext(person.getID() + "");
      // Bind the user's sessionID to this context
      thisUsersContext.bind("sessionID", session.getId());
      // Create a subcontext for the user's channel instance IDs
      Context thisUsersChannelIDs = (Context)thisUsersContext.createSubcontext("channel-ids");
      // Get the list of channels in the user's layout
      NodeList channelNodes = userLayout.getElementsByTagName("channel");
      Node fname = null;
      Node instanceid = null;
      // Parse through the channels and populate the JNDI
      for (int i = 0; i < channelNodes.getLength(); i++) {
        // Attempt to get the fname and instance ID from the channel
        fname = channelNodes.item(i).getAttributes().getNamedItem("fname");
        instanceid = channelNodes.item(i).getAttributes().getNamedItem("ID");
        if (fname != null && instanceid != null) {
          //System.out.println("fname found -> " + fname);
          // Create a new composite name from the fname
          CompositeName cname = new CompositeName(fname.getNodeValue());
          // Get a list of the name components
          Enumeration e = cname.getAll();
          // Get the root of the context
          Context nextContext = (Context)thisUsersContext.lookup("channel-ids");
          // Add all of the subcontexts in the fname
          String subContextName = new String();
          while (e.hasMoreElements()) {
            subContextName = (String)e.nextElement();
            if (e.hasMoreElements()) {
              // Bind a new sub context if the current name component is not the leaf
              nextContext = nextContext.createSubcontext(subContextName);
            } 
            else {
              //System.out.println("Binding " + instanceid.getNodeValue() + " to " + nextContext.getNameInNamespace() + "/" + subContextName);
              nextContext.bind(subContextName, instanceid.getNodeValue());
            }
          }
        }
      }
    } catch (Exception ne) {
      throw  new PortalNamingException(ne.getMessage());
    }
  }

  /**
   * Get the uPortal JNDI context
   * @return 
   * @exception NamingException
   */
  private static Context getContext () throws NamingException {
    Hashtable environment = new Hashtable(5);
    // Set up the path
    environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jasig.portal.jndi.PortalInitialContextFactory");
    Context ctx = new InitialContext(environment);
    return  (ctx);
  }

  /**
   * This class will be bound to the user's session when they log in. When the user's session is expired this
   * object should be unbound and will clean up all user specific objects in JNDI. Note: It's possible that
   * not all servlet containers properly unbind objects from the session when it expires!
   */
  private static class JNDISessionListener
      implements HttpSessionBindingListener {

    /**
     * put your documentation comment here
     * @param     HttpSessionBindingEvent bindingEvent
     */
    public void valueBound (HttpSessionBindingEvent bindingEvent) {
      LogService.instance().log(LogService.INFO, "JNDISessionListener bound for: " + bindingEvent.getSession().getId());
    }

    /**
     * This method is called when the JNDISessionListener is unbound from a user's session. This should
     * only happen when the users session is either destroyed or expires. Note: This method may need synchronization!
     * If a user logs in and out quickly there may be problems with things not happening in the correct order.
     * @param     HttpSessionBindingEvent bindingEvent
     */
    public void valueUnbound (HttpSessionBindingEvent bindingEvent) {
      LogService.instance().log(LogService.INFO, "JNDISessionListener unbound for: " + bindingEvent.getSession().getId());
      Context context = null;
      try {
        // Get the portal JNDI context
        context = getContext();
      } catch (NamingException ne) {
        LogService.instance().log(LogService.ERROR, "JNDISessionListener.valueUnbound(): Could not get portal context " + 
            ne.getMessage());
        return;
      }
      Context sessionsContext = null;
      try {
        // Get the context that keeps track of user's sessions
        sessionsContext = (Context)context.lookup("sessions");
      } catch (NamingException ne) {
        LogService.instance().log(LogService.ERROR, "JNDISessionListener.valueUnbound(): Could not get sessions context "
            + ne.getMessage());
        return;
      }
      if (sessionsContext == null) {
        return;
      }
      IPerson person = null;
      try {
        // Get the person object that is bound to this session ID
        person = (IPerson)sessionsContext.lookup(bindingEvent.getSession().getId());
      } catch (NamingException ne) {
        LogService.instance().log(LogService.ERROR, "JNDISessionListener.valueUnbound(): Could not get IPerson object " + 
            ne.getMessage());
        return;
      }
      try {
        // Remove the session context from JNDI
        sessionsContext.unbind(bindingEvent.getSession().getId());
      } catch (NamingException ne) {
        LogService.instance().log(LogService.ERROR, "JNDISessionListener.valueUnbound(): Problem unbinding user's session "
            + ne.getMessage());
        return;
      }
      Context usersContext = null;
      try {
        // Remove the user's information from the user's context
        usersContext = (Context)context.lookup("users");
      } catch (NamingException ne) {
        LogService.instance().log(LogService.ERROR, "JNDISessionListener.valueUnbound(): Could not find /users context "
            + ne.getMessage());
        return;
      }
      try {
        // Unbind all of the users JNDI information
        usersContext.unbind(person.getID() + "");
      } catch (NamingException ne) {
        LogService.instance().log(LogService.ERROR, "JNDISessionListener.valueUnbound(): Problem unbinding user's context "
            + ne.getMessage());
        return;
      }
    }
  }
}



