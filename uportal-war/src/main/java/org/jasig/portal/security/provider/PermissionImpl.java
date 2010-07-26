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

package org.jasig.portal.security.provider;

import java.io.Serializable;
import java.util.Date;

import org.jasig.portal.security.IPermission;


/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 * @author Dan Ellentuck
 * @version $Revision$
 */
public class PermissionImpl implements IPermission, Serializable {
  private String m_owner = null;
  private String m_principal = null;
  private String m_activity = null;
  private String m_target = null;
  private String m_type = null;
  private Date m_effective = null;
  private Date m_expires = null;
  private static final long serialVersionUID = 1L;

  /**
   * This constructor ensures that all Permission objects are created with an owner specified.
   * @param owner
   */
  public PermissionImpl (String owner) {
    // Make sure to call the constructor that stores the owner
    super();
    m_owner = owner;
  }
  /**
   * Returns the token that represents the activity associated with this <code>IPermission</code>.
   * @return String
   */
  public String getActivity () {
    return  (m_activity);
  }
  /**
   * Returns the effective date that was set for this Permission.
   * @return the effective date that was set for this Permission
   */
  public Date getEffective () {
    return  (m_effective);
  }
  /**
   * Returns the expires date that was set for this Permission.
   * @return the expires date that was set for this Permission
   */
  public Date getExpires () {
    return  (m_expires);
  }
  /**
   * Returns the owner of this Permission
   * @return the owner of this Permission
   */
  public String getOwner () {
    return  (m_owner);
  }
  /**
   * Returns the token that represents the <code>IAuthorizationPrincipal</code>
   * associated with this <code>IPermission</code>.
   * @return String
   */
  public String getPrincipal () {
    return  (m_principal);
  }
  /**
   * Returns the token that represents the target associated with this <code>IPermission</code>.
   */
  public String getTarget () {
    return  (m_target);
  }
  /**
   * Returns the type of permission that this is, generally GRANT or DENY
   * @return String
   */
  public String getType () {
    return  (m_type);
  }
  /**
   * Specifies the token that represents the activity associated with this <code>IPermission</code>.
   * @param activity String
   */
  public void setActivity (String activity) {
    m_activity = activity;
  }
  /**
   * Specifies the date that this <code>IPermission</code> will become effective.
   * @param effective java.util.Date
   */
  public void setEffective (Date effective) {
    m_effective = effective;
  }
  /**
   * Specifies the date that this <code>IPermission</code> will expire.
   * @param expires java.util.Date
   */
  public void setExpires (Date expires) {
    m_expires = expires;
  }
  /**
   * Specifies the token that represents the <code>IAuthorizationPrincipal</code>
   * associated with this <code>IPermission</code>.
   * @param newPrincipal String
   */
  public void setPrincipal (String newPrincipal) {
    m_principal = newPrincipal;
  }
  /**
   * Specifies the token that represents the target associated with this <code>IPermission</code>.
   * @param target String
   */
  public void setTarget (String target) {
    m_target = target;
  }
  /**
   * Sets the <code>IPermission</code> type.
   * @param type String
   */
  public void setType (String type) {
    m_type = type;
  }
/**
 * @return java.lang.String
 */
public String toString() {
    StringBuffer buff = new StringBuffer("Permission on ");
    buff.append(getOwner());
    buff.append(" for ");
    buff.append(getPrincipal());
    buff.append(" (");
    buff.append(getActivity());
    buff.append(")");
    return buff.toString();
}
}
