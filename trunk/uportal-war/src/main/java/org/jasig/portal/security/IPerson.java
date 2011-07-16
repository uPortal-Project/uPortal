/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package  org.jasig.portal.security;

import java.io.Serializable;
import java.security.Principal;
import java.util.Enumeration;
import java.util.List;
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
    * @return The userName for the user.
    */
    public String getUserName();

    /**
     * @param userName The userName to set for the user.
     */
    public void setUserName(String userName);

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
   * @param key
   * @param values
   */
  public void setAttribute(String key, List<Object> values);

  /**
   * Associates attributes with the user
   * @param attrs
   */
  public void setAttributes (Map<String, List<Object>> attrs);

  /**
   * Gets all of the attributes associated with the user
   * @return all of the attributes associated with the user
   */
  public Enumeration<List<Object>> getAttributes ();

  public Map<String,List<Object>> getAttributeMap();
  
  /**
   * Returns the names of all of the attributes stored for the user
   * @return names of all of the attributes stored for the user
   */
  public Enumeration<String> getAttributeNames ();

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



