/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.Enumeration;
import java.util.Map;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;

/**
 * @author Bernie Durfee, bdurfee@interactivebusiness.com
 * @version $Revision$
 */
public interface IPerson extends Principal, IAdditionalDescriptor, IBasicEntity, Serializable {

  /**
   * String used as a key for the eduPerson username attribute.
   */
  public static final String USERNAME = "username";

  /**
   * The default ID for person objects.
   */
  public static final int UNDEFINED_ID = -1;
  
  /**
   * The user id for guest users.
   */
  public static final int GUEST_ID = 1;
  
  /**
   * The user id for the special system user.
   */
  public static final int SYSTEM_USER_ID = 0;
  
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
   * Gets multiple values of an attribute associated with the user
   * @param key
   * @return attributes associated with the user
   */
  public Object[] getAttributeValues (String key);

  /**
   * Associates an attribute with the user
   * @param key
   * @param value
   */
  public void setAttribute (String key, Object value);

  /**
   * Associates attributes with the user
   * @param attrs
   */
  public void setAttributes (Map attrs);

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

  /**
   * Explicitly set the entity identifier
   * The default implementation enforces a one time setting
   * so that the value can't be changed once explicitly set.
   * @param ei
   */
  public void setEntityIdentifier(EntityIdentifier ei);
}



