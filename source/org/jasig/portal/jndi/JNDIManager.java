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

package org.jasig.portal.jndi;

import java.util.Hashtable;
import java.util.Enumeration;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.naming.CompositeName;

import javax.servlet.http.HttpSession;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

/**
 * JNDIManager
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 */
public class JNDIManager
{
  public JNDIManager()
  {
  }

  public static void initializePortalContext()
  {
    try
    {
      Context context = getContext();

      // Create a subcontext for portal-wide services
      context.createSubcontext("services");

      // Bind in the logger service
      LogService logger = LogService.instance();
      context.bind("/services/logger", logger);

      // Create a subcontext for user specific bindings
      context.createSubcontext("users");
    }
    catch(Exception e)
    {
      e.printStackTrace();
    }
  }

  public static void initializeUserContext(Document userLayout, String sessionID, IPerson person)
    throws PortalNamingException
  {
    try
    {
      // Throw an exception if the person object is not found
      if(person == null)
      {
        throw new PortalNamingException("JNDIManager.initializeUserContext() - Cannot find person object!");
      }

      // Throw an exception if the user's layout cannot be found
      if(userLayout == null)
      {
        throw new PortalNamingException("JNDIManager.initializeUserContext() - Cannot find user's layout!");
      }

      // Get the portal wide context
      Context context = getContext();

      // Get the list of channels in the user's layout
      NodeList channelNodes = userLayout.getElementsByTagName("channel");

      Node fname      = null;
      Node instanceid = null;

      // Parse through the channels and populate the JNDI
      for(int i = 0; i < channelNodes.getLength(); i++)
      {
        fname      = channelNodes.item(i).getAttributes().getNamedItem("fname");
        instanceid = channelNodes.item(i).getAttributes().getNamedItem("ID");

        if(fname != null && instanceid != null)
        {
          // Create a new composite name from the fname
          CompositeName cname = new CompositeName(fname.getNodeValue());

          // Get a list of the name components
          Enumeration e = cname.getAll();

          // Get the root of the context
          Context nextContext = (Context)context.lookup("");

          // Add all of the subcontexts in the fname
          while(e.hasMoreElements())
          {
            nextContext = nextContext.createSubcontext((String)e.nextElement());
          }

          // Bind the instance ID of the channel as a leaf
          nextContext.rebind(cname.get(cname.size() - 1), instanceid.getNodeValue());
        }
      }
    }
    catch(Exception ne)
    {
      throw new PortalNamingException(ne.getMessage());
    }
  }

  private static Context getContext()
    throws NamingException
  {
    Hashtable environment = new Hashtable(5);

    // Set up the path
    environment.put(Context.INITIAL_CONTEXT_FACTORY, "org.jasig.portal.jndi.PortalInitialContextFactory");
    Context ctx = new InitialContext(environment);

    return(ctx);
  }
}
