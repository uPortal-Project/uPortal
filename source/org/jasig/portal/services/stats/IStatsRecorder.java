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

package org.jasig.portal.services.stats;

import org.jasig.portal.security.IPerson;
import org.jasig.portal.ChannelDefinition;

/**
 * <p>Gathers portal usage statistics such as user logins.
 * All the implementors of this interface should handle
 * their own exceptions.</p>
 * <p>In a multiple-server environment,
 * the <code>IStatsRecorder</code> implementation will be
 * called by the portal on each JVM.  For such a set-up,
 * the <code>IStatsRecorder</code> implementation should
 * probably aggregate statistics on a single server.</p>
 * 
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public interface IStatsRecorder {
  /**
   * Called when user authenticates successfully.
   * @param person, the IPerson object
   */
  public void recordLogin(IPerson person);

  /**
   * Called when user logs out.
   * @param person, the IPerson object
   */
  public void recordLogout(IPerson person);
  
  
  /**
   * Called when a new session is created for a user.
   * @param person, the IPerson object
   */
  public void recordSessionCreated(IPerson person);
  
  /**
   * Called when the user's session is destroyed.  This
   * occurs when the user logs out or his/her session
   * simply times out.
   * @param person, the IPerson object
   */
  public void recordSessionDestroyed(IPerson person);
  
  /**
   * Called when a user publishes a channel
   * @param person, the person pubishing the channel
   * @param channelDef, the channel being published
   */
  public void recordChannelDefinitionPublished(IPerson person, ChannelDefinition channelDef);
 
  /**
   * Called when a user modifies an existing channel
   * @param person, the person modifying the channel
   * @param channelDef, the channel being modified
   */
  public void recordChannelDefinitionModified(IPerson person, ChannelDefinition channelDef);

  /**
   * Called when a user removes an existing channel
   * @param person, the person removing the channel
   * @param channelDef, the channel being removed
   */
  public void recordChannelDefinitionRemoved(IPerson person, ChannelDefinition channelDef);
    
}
