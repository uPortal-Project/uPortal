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

import java.util.Enumeration;
import java.util.Hashtable;

/**
 * <p>This is an implementation of an AdditionalDescriptor that contains a user's
 * record from the LDAP server.
 *
 * @author Adam Rybicki
 */

public class PersonImpl implements IPerson
{
  protected Hashtable m_Attributes = null;
  protected String m_FullName = null;
  protected String m_ID = null;
  
  /**
   * Returns an attribute for a key.  For multivalue attributes, this will be
   * a <code>java.util.Vector</code>.  For objects represented as strings,
   * a <code>java.lang.String</code> will be returned.  Binary values will
   * be represented a byte arrays.
   * @param key Attribute's name.
   * @return Value of an attribute identified by the key.
   */
  public Object getAttribute (String key)
  {
    if (m_Attributes == null)
      return null;
      
    return m_Attributes.get (key);
  }
  
  /**
   * Returns a <code>java.util.Enumeration</code> of all the attribute values.
   * @return <code>java.util.Enumeration</code> of the attributes.
   */
  public Enumeration getAttributes ()
  {
    if (m_Attributes == null)
      return null;
      
    return m_Attributes.elements();
  }
  
  /**
   * Sets the specified attribute to a value.
   * @param key Attribute's name
   * @param value Attribute's value
   */
  public void setAttribute (String key, Object value)
  {
    if (m_Attributes == null)
      m_Attributes = new Hashtable ();
      
    m_Attributes.put (key, value);
  }
  
  /**
   * Removes the specified attribute.
   * @param key Attribute's name
   * @return the value of the removed object or null if there was nothing to remove
   */
  public Object removeAttribute (String key)
  {
    if (m_Attributes == null)
      return null;
      
     return m_Attributes.remove(key);
  }

  /**
   * Returns the user's ID that was used for authentication.
   * @return User's ID.
   */
  public String getID ()
  {
    return m_ID;
  }
  
  /**
   * Sets the user's ID.
   * @param sID User's ID as supplied for authentication
   */
  public void setID (String sID)
  {
    m_ID = sID;
  }

  /**
   * Returns the user's name that was established during authentication.
   * @return User's name.
   */
  public String getFullName ()
  {
    return m_FullName;
  }
  
  /**
   * Sets the user's full name.
   * @param sFullName User's name as established during authentication
   */
  public void setFullName (String sFullName)
  {
    m_FullName = sFullName;
  }
}
