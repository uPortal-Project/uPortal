/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.webservices;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.PortalEvent;
import org.w3c.dom.Element;

/**
 * <p>A remote channel web service interface on which to base the WSDL</p>
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public interface IRemoteChannel {

  /**
   * Authenticates user and establishes a session.
   * @param username the user name of the user
   * @param password the user's password
   * @throws java.lang.Exception if there was a problem trying to authenticate
   */
  public void authenticate(String username, String password) throws Exception;


  /**
   * Unauthenticates a user, killing the session.
   * @throws java.lang.Exception if there was a problem trying to logout
   */
  public void logout() throws Exception;


  /**
   * Establishes a channel instance which the webservice client will communicate with.
   * @param fname an identifier for the channel unique within a particular portal implementation
   * @return instanceId an identifier for the newly-created channel instance
   * @throws java.lang.Exception if the channel cannot be located
   */
  public String instantiateChannel(String fname) throws Exception;

  /**
   * Asks the channel to render content and return it as an XML Element.
   * The content will be well-formed XML which the client must serialize.
   * @param instanceId an identifier for the channel instance returned by instantiateChannel()
   * @param runtimeData the channel runtime data including request parameters
            headers, cookies, etc.
   * @return xml an XML element representing the channel's output
   * @throws java.lang.Exception if the channel cannot respond with the expected rendering
   */
  public Element renderChannel(String instanceId, ChannelRuntimeData runtimeData) throws Exception;

  /**
   * Passes portal events to the channel.
   * @param instanceId an identifier for the channel instance returned by instantiateChannel()   
   * @param event a portal event
   * @throws java.lang.Exception if the channel cannot receive its event   
   */
  public void receiveEvent(String instanceId, PortalEvent event) throws Exception;                               
                               
  /**
   * Indicates to the portal that the web services client is finished
   * talking to the channel instance.
   * @param instanceId an identifier for the channel instance returned by instantiateChannel()
   * @throws java.lang.Exception if the channel cannot be freed
   */
  public void freeChannel(String instanceId) throws Exception;

}