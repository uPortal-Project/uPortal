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


package  org.jasig.portal.security.provider;

import java.util.Enumeration;
import java.util.Hashtable;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.security.ISecurityContext;


/**
 * <p>This is an implementation of an AdditionalDescriptor that contains a user's
 * record from the LDAP server.
 *
 * @author Adam Rybicki, arybicki@interactivebusiness.com
 * @version $Revision$
 */
public class PersonImpl
    implements IPerson {
  protected Hashtable m_Attributes = null;
  protected String m_FullName = null;
  protected int m_ID = -1;
  protected ISecurityContext m_securityContext = null;
  protected EntityIdentifier m_eid= new EntityIdentifier(null,IPerson.class);


  public void setSecurityContext (ISecurityContext securityContext) {
    m_securityContext = securityContext;
  }


  public ISecurityContext getSecurityContext () {
    return  (m_securityContext);
  }

  /**
   * Returns an attribute for a key.  For multivalue attributes, this will be
   * a <code>java.util.Vector</code>.  For objects represented as strings,
   * a <code>java.lang.String</code> will be returned.  Binary values will
   * be represented a byte arrays.
   * This reference implementation supports the attribute: Email
   * corresponding to the eduPerson attribute: mail.
   * @param key Attribute's name.
   * @return Value of an attribute identified by the key.
   */
  public Object getAttribute (String key) {
    if (m_Attributes == null)
      return  null;
    return  m_Attributes.get(key);
  }

  /**
   * Returns a <code>java.util.Enumeration</code> of all the attribute values.
   * @return <code>java.util.Enumeration</code> of the attributes.
   */
  public Enumeration getAttributes () {
    if (m_Attributes == null)
      return  null;
    return  m_Attributes.elements();
  }

  /**
   * Returns an enumeration of all of the attribute names associated with the user
   * @return enumeration of all of the attribute names associated with the user
   */
  public Enumeration getAttributeNames () {
    if (m_Attributes == null) {
      return  (null);
    }
    else {
      return  (m_Attributes.keys());
    }
  }

  /**
   * Sets the specified attribute to a value.
   * This reference implementation supports the attribute: Email
   * corresponding to the eduPerson attribute: mail.
   * 
   * Reference impementation checks for the setting of the username attribute
   * and updates the EntityIdentifier accordingly
   * 
   * @param key Attribute's name
   * @param value Attribute's value
   */
  public void setAttribute (String key, Object value) {
    if (m_Attributes == null)
      m_Attributes = new Hashtable();
    m_Attributes.put(key, value);
    if (key.equals(IPerson.USERNAME)){
       m_eid = new EntityIdentifier(String.valueOf(value),IPerson.class);
    }
  }

  /**
   * Returns the user's ID that was used for authentication.
   * Does not correlate to any eduPerson attribute but is the key
   * for user data in uPortal reference rdbms.
   * @return User's ID.
   */
  public int getID () {
    return  m_ID;
  }

  /**
   * Sets the user's ID.
   * @param sID User's ID as supplied for authentication
   * Does not correlate to any eduPerson attribute but is the key
   * for user data in uPortal reference rdbms.
   */
  public void setID (int sID) {
    m_ID = sID;
  }

  /**
   * Returns the user's name that was established during authentication.
   * Correlates to cn (common name) in the eduPerson 1.0 specification.
   * @return User's name.
   */
  public String getFullName () {
    return  m_FullName;
  }

  /**
   * Sets the user's full name.
   * @param sFullName User's name as established during authentication
   * Correlates to cn (common name) in the eduPerson 1.0 specification.
   */
  public void setFullName (String sFullName) {
    m_FullName = sFullName;
  }

  /**
   * This implementation of a person object is considered a guest if:
   *   UID = 1
   *   Unauthenticated
   * @return true if person is a guest
   */
  public boolean isGuest () {
    if (getID() == 1 && !m_securityContext.isAuthenticated()) {
      return  (true);
    }
    else {
      return  (false);
    }
  }
  
  /**
   * Returns an EntityIdentifier for this person.  The key contains the value
   * of the eudPerson username attribute, or null 
   * 
   * @return EntityIdentifier with attribute 'username' as key.
   */
  public EntityIdentifier getEntityIdentifier(){
    return m_eid; 
  }
}



