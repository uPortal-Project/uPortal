/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.security;

import java.util.Date;

/**
 * @author Bernie Durfee (bdurfee@interactivebusiness.com)
 * @author Dan Ellentuck
 * @version $Revision$
 */
public interface IPermission {
  /*
    Activity names for Permissions whose targets are portlets.
  */
  public String PORTLET_MANAGER_ACTIVITY = "MANAGE";
  public String PORTLET_SUBSCRIBER_ACTIVITY = "SUBSCRIBE";
  
  /*
   * Portlet subscribe permissions by lifecycle state 
   */
  public static final String PORTLET_SUBSCRIBER_CREATED_ACTIVITY = "SUBSCRIBE_CREATED";
  public static final String PORTLET_SUBSCRIBER_APPROVED_ACTIVITY = "SUBSCRIBE_APPROVED";
  public static final String PORTLET_SUBSCRIBER_EXPIRED_ACTIVITY = "SUBSCRIBE_EXPIRED";

    /**
     * Portlet subscribe permission to view ("browse") marketplace entry.
     * @since uPortal 4.1
     */
    public static final String PORTLET_BROWSE_ACTIVITY = "BROWSE";

  
  /*
   * Portlet render permissions by lifecycle state.  These permissions are not
   * actually used in the codebase yet and are included here for future planning 
   * purposes only.
   */
  public static final String PORTLET_RENDERER_CREATED_ACTIVITY = "RENDER_CREATED";
  public static final String PORTLET_RENDERER_APPROVED_ACTIVITY = "RENDER_APPROVED";
  public static final String PORTLET_RENDERER_PUBLISHED_ACTIVITY = "RENDER_PUBLISHED";
  public static final String PORTLET_RENDERER_EXPIRED_ACTIVITY = "RENDER_EXPIRED";

  /*
   * Portlet management permissions by portlet type.
   */
  public static final String PORTLET_MANAGER_SELECT_PORTLET_TYPE = "SELECT_PORTLET_TYPE";

  /*
   * Portlet management permissions by lifecycle state.
   */
  public static final String PORTLET_MANAGER_CREATED_ACTIVITY = "MANAGE_CREATED";
  public static final String PORTLET_MANAGER_APPROVED_ACTIVITY = "MANAGE_APPROVED";
  public static final String PORTLET_MANAGER_EXPIRED_ACTIVITY = "MANAGE_EXPIRED";
  /**
   * @since 4.2
   */
  public static final String PORTLET_MANAGER_MAINTENANCE_ACTIVITY = "MANAGE_MAINTENANCE";

  /*
   * All management permissions in one handy array
   */
  public static final String[] PORTLET_MANAGER_MANAGE_ACTIVITIES = new String[] {
      PORTLET_MANAGER_CREATED_ACTIVITY, PORTLET_MANAGER_APPROVED_ACTIVITY,
      PORTLET_MANAGER_ACTIVITY, PORTLET_MANAGER_EXPIRED_ACTIVITY,
      PORTLET_MANAGER_MAINTENANCE_ACTIVITY
  };

  /*
   * PortletMode permissions
   */
  public static final String PORTLET_MODE_CONFIG = "PORTLET_MODE_CONFIG";

  /*
   * UP_GROUP (GaP) Permissions
   */

  public static final String VIEW_GROUP_ACTIVITY = "VIEW_GROUP";
  public static final String CREATE_GROUP_ACTIVITY = "CREATE_GROUP";
  public static final String DELETE_GROUP_ACTIVITY = "DELETE_GROUP";
  public static final String EDIT_GROUP_ACTIVITY = "EDIT_GROUP";

  /*
    Permission types.  At present only 2, but that could change.
  */
  public static final String PERMISSION_TYPE_GRANT = "GRANT";
  public static final String PERMISSION_TYPE_DENY = "DENY";

  /*
   * Permission Owner Strings
   */

  /**
   * A String representing the uPortal framework, used, for example, for 
   * Permission.owner when the framework grants a Permission.
   */
  public static final String PORTAL_SYSTEM = "UP_SYSTEM";

  /**
   * Represents the GaP subsystem as a permissions owner
   */
  public static final String PORTAL_GROUPS = "UP_GROUPS";

  public static final String PORTAL_PUBLISH = "UP_PORTLET_PUBLISH";

  public static final String PORTAL_SUBSCRIBE = "UP_PORTLET_SUBSCRIBE";


  /*
    A String which, when concatentated with a portlet id, represents a portal
    portlet.  Used, for example, for Permission.target when the portal framework
    grants a Permission to perform some activity on a portlet.
    See PermissionHelper for a convenience method for correctly using this.
  */
  public static final String PORTLET_PREFIX = "PORTLET_ID.";    

  public static final String ALL_PORTLET_TYPES = "ALL_PORTLET_TYPES";

  public static final String ALL_PORTLETS_TARGET = "ALL_PORTLETS";

  public static final String ALL_GROUPS_TARGET = "ALL_GROUPS";

  public static final String ALL_CATEGORIES_TARGET = "ALL_CATEGORIES";

  public static final String ALL_PERMISSIONS_ACTIVITY = "ALL_PERMISSIONS";

  public static final String ALL_TARGET = "ALL";

  /**
   * Gets the activity associated with this <code>IPermission</code>.
   * @return String
   */

  public String getActivity ();

  /**
   * Gets that date that this <code>IPermission</code> should become effective on.
   * @return date that this <code>IPermission</code> should become effective on
   */
  public Date getEffective ();

  /**
   * Gets the date that this <code>IPermission</code> should expire on.
   * @return date that this <code>IPermission</code> should expire on
   */
  public Date getExpires ();

  /**
   * Returns the owner of this <code>IPermission</code>.
   * @return owner of this <code>IPermission</code>
   */
  public String getOwner ();

  /**
   * Gets the target associated with this <code>IPermission</code>.
   * @return target associated with this <code>IPermission</code>
   */
  public String getTarget ();

  /**
   * Returns the <code>Permission</code> type.
   */
  public String getType ();

  /**
   * Sets the activity associated with this <code>IPermission</code>.
   * @param activity String
   */
  public void setActivity (String activity);

  /**
   * Sets the date that this <code>IPermission</code> should become effective on.
   * @param effective java.util.Date
   */
  public void setEffective (Date effective);

  /**
   * Sets the date that this <code>IPermission</code> should expire on.
   * @param expires java.util.Date
   */
  public void setExpires (Date expires);

  /**
   * Sets the target associated with this <code>IPermission</code>.
   * @param target
   */
  public void setTarget (String target);

  /**
   * Sets the <code>IPermission</code> type.
   * @param type String
   */
  public abstract void setType (String type);

  /**
   * Returns a String representing the <code>IAuthorizationPrincipal</code> associated
   * with this <code>IPermission</code>.
   * @return IAuthorizationPrincipal associated with this IPermission
   */
  public String getPrincipal();

  /**
   * Sets the principal String representing the <code>IAuthorizationPrincipal</code>
   * associated with this <code>IPermission</code>.
   * @param newPrincipal String
   */
  public void setPrincipal (String newPrincipal);
}
