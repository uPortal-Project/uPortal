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

package  org.jasig.portal.security;

import java.util.Enumeration;

import org.jasig.portal.IBasicEntity;


/**
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 */
public interface IPerson extends IAdditionalDescriptor, IBasicEntity, java.io.Serializable
{
  // string used as a key for the eduPerson username attribute
  public static final String USERNAME = "username";

  /**
   * Sets the ID of the user
   * @param sID
   */
  public void setID (int sID);



  /**
   * Gets the ID of the user
   * @return ID of the user
   */
  public int getID ();



  /**
   * Sets the full name of the user
   * @param sFullName
   */
  public void setFullName (String sFullName);



  /**
   * Gets the full name of the user
   * @return full name of the user
   */
  public String getFullName ();



  /**
   * Gets an attribute associated with the user
   * @param key
   * @return attribute associated with the user
   */
  public Object getAttribute (String key);



  /**
   * Associates an attribute with the user
   * @param key
   * @param value
   */
  public void setAttribute (String key, Object value);



  /**
   * Gets all of the attributes associated with the user
   * @return all of the attributes associated with the user
   */
  public Enumeration getAttributes ();



  /**
   * Returns the names of all of the attributes stored for the user
   * @return names of all of the attributes stored for the user
   */
  public Enumeration getAttributeNames ();



  /**
   * Associates a security context object with the user
   * @param securityContext
   */
  public void setSecurityContext (ISecurityContext securityContext);



  /**
   * Gets the security context object associated with the user
   * @return security context object associated with the user
   */
  public ISecurityContext getSecurityContext ();



  /**
   * Checks to see if this user is considered a guest
   * @return true if user is considered a guest
   */
  public boolean isGuest ();
}



