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

import org.jasig.portal.Logger;

import org.jasig.portal.security.PortalSecurityException;
import org.jasig.portal.security.IRole;

import java.util.Hashtable;
import java.util.Enumeration;

/**
 * @author Bernie Durfee
 */
public class RoleImpl implements IRole
{
  private String m_roleTitle;
  private Hashtable m_roleAttributes;

  public RoleImpl(String roleTitle)
  {
    m_roleTitle = new String(roleTitle);

    m_roleAttributes = new Hashtable();
  }

  // Get the title of this role
  public String getRoleTitle()
  {
    return(m_roleTitle);
  }

  // Get an attribute for this role
  public Object getAttribute(String key)
  {
    return(m_roleAttributes.get(key));
  }

  // Set an attribute for this role
  public boolean setAttribute(String key, Object value)
  {
    if(key == null)
    {
      return(false);
    }
    else
    {
      m_roleAttributes.put(key, value);
      return(true);
    }
  }

  // Get all attributes for this role
  public Enumeration getAttributes()
  {
    if(m_roleAttributes == null)
    {
      return(null);
    }
    else
    {
      return(m_roleAttributes.keys());
    }
  }
}