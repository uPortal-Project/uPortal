/**
 * Copyright (c) 2000 The JA-SIG Collaborative.  All rights reserved.
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

import java.util.Vector;

import org.jasig.portal.security.IRole;
import org.jasig.portal.security.IPerson;

/**
 * @author Bernie Durfee
 */
public interface IAuthorization
{
  public boolean isUserInRole(IPerson person, String sRoleTitle);
  public boolean isUserInRole(IPerson person, IRole iRole);
  public Enumeration getAllRoles();

  public int setChannelRoles(int iChannelID, Enumeration eRoles);
  public boolean canUserPublish(IPerson person);

  // For the subscribe mechanism to use
  public Vector  getAuthorizedChannels(IPerson person);
  public boolean canUserSubscribe(IPerson person, int channelID);

  // For the render mechanism to use
  public boolean canUserRenderChannel(IPerson person, int channelID);

  // For the administration mechanism to use
  public Vector getUserRoles(IPerson person);
  public void addUserRoles(IPerson person, Vector roles);
  public void removeUserRoles(IPerson person, Vector roles);
}