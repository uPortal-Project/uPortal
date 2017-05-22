/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.security;

import java.util.Date;

/**
 */
public interface IPermission {

    /*
     * Portlet subscribe permissions listed
     * hierarchically by lifecycle state
     */

    /**
     * Allows the user to view or add to his or her layout a portlet that is in the <code>CREATED
     * </code> lifecycle state.
     */
    String PORTLET_SUBSCRIBER_CREATED_ACTIVITY = "SUBSCRIBE_CREATED";
    /**
     * Allows the user to view or add to his or her layout a portlet that is in the <code>APPROVED
     * </code> lifecycle state.
     */
    String PORTLET_SUBSCRIBER_APPROVED_ACTIVITY = "SUBSCRIBE_APPROVED";
    /**
     * The standard <code>SUBSCRIBE</code> activity. Allows the user to view or add to his or her
     * layout a portlet that is in the <code>PUBLISHED</code> lifecycle state.
     */
    String PORTLET_SUBSCRIBER_ACTIVITY = "SUBSCRIBE";
    /**
     * Allows the user to view or add to his or her layout a portlet that is in the <code>EXPIRED
     * </code> lifecycle state.
     */
    String PORTLET_SUBSCRIBER_EXPIRED_ACTIVITY = "SUBSCRIBE_EXPIRED";

    /**
     * Portlet subscribe permission to view ("browse") marketplace entry.
     *
     * @since 4.1
     */
    String PORTLET_BROWSE_ACTIVITY = "BROWSE";

    /**
     * Permission to favorite/star a portlet.
     *
     * @since 5.0
     */
    String PORTLET_FAVORITE_ACTIVITY = "FAVORITE";

    /*
     * Portlet management permissions by portlet type.
     */
    String PORTLET_MANAGER_SELECT_PORTLET_TYPE = "SELECT_PORTLET_TYPE";

    /*
     * Portlet management permissions listed
     * hierarchically by lifecycle state
     */

    /**
     * Allows the user to edit the publication metadata of a portlet that is in the <code>CREATED
     * </code> lifecycle state.
     */
    String PORTLET_MANAGER_CREATED_ACTIVITY = "MANAGE_CREATED";
    /**
     * Allows the user to edit the publication metadata of a portlet that is in the <code>APPROVED
     * </code> lifecycle state.
     */
    String PORTLET_MANAGER_APPROVED_ACTIVITY = "MANAGE_APPROVED";
    /**
     * The standard <code>MANAGE</code> activity. Allows the user to edit the publication metadata
     * of a portlet that is in the <code>PUBLISHED</code> lifecycle state.
     */
    String PORTLET_MANAGER_ACTIVITY = "MANAGE";
    /**
     * Allows the user to edit the publication metadata of a portlet that is in the <code>EXPIRED
     * </code> lifecycle state.
     */
    String PORTLET_MANAGER_EXPIRED_ACTIVITY = "MANAGE_EXPIRED";
    /**
     * Allows the user to edit the publication metadata of a portlet that is in the <code>
     * MAINTENANCE</code> lifecycle state.
     *
     * @since 4.2
     */
    String PORTLET_MANAGER_MAINTENANCE_ACTIVITY = "MANAGE_MAINTENANCE";

    /** All management permissions in one handy array. Used within the edit-portlet flow. */
    @SuppressWarnings("ucd")
    String[] PORTLET_MANAGER_MANAGE_ACTIVITIES =
            new String[] {
                PORTLET_MANAGER_CREATED_ACTIVITY, PORTLET_MANAGER_APPROVED_ACTIVITY,
                PORTLET_MANAGER_ACTIVITY, PORTLET_MANAGER_EXPIRED_ACTIVITY,
                PORTLET_MANAGER_MAINTENANCE_ACTIVITY
            };

    /*
     * PortletMode permissions
     */
    String PORTLET_MODE_CONFIG = "PORTLET_MODE_CONFIG";

    /*
     * UP_GROUP (GaP) Permissions
     */

    String VIEW_GROUP_ACTIVITY = "VIEW_GROUP";
    String CREATE_GROUP_ACTIVITY = "CREATE_GROUP";
    String DELETE_GROUP_ACTIVITY = "DELETE_GROUP";
    String EDIT_GROUP_ACTIVITY = "EDIT_GROUP";

    /** Activity string for adding a tab to your personal layout */
    String ADD_TAB_ACTIVITY = "ADD_TAB";

    /** Non-owner-specific view activity (used by ERROR_PORTLET) */
    String VIEW_ACTIVITY = "VIEW";

    /**
     * Determines whether a user is visible within the portal.
     */
    String VIEW_USER_ACTIVITY = "VIEW_USER";

    /**
     * Governs which user attributes are visible within the portal, applies
     * to attributes of others as well as one's own attributes.
     */
    String VIEW_USER_ATTRIBUTE_ACTIVITY = "VIEW_USER_ATTRIBUTE";

    /**
     * Governs additional visibility of one's own user attributes.  When it comes to their own
     * attributes, users may view those for which they have either
     * <code>VIEW_USER_ATTRIBUTE_ACTIVITY</code> or <code>VIEW_OWN_USER_ATTRIBUTE_ACTIVITY</code>
     * permission.
     *
     * @since 5.0
     */
    String VIEW_OWN_USER_ATTRIBUTE_ACTIVITY = "VIEW_OWN_USER_ATTRIBUTE";

    /**
     * Governs the ability to become another user through the User Manager.
     */
    String IMPERSONATE_USER_ACTIVITY = "IMPERSONATE";

    String VIEW_PERMISSIONS_ACTIVITY = "VIEW_PERMISSIONS";
    String EDIT_PERMISSIONS_ACTIVITY = "EDIT_PERMISSIONS";

    /*
     * These two are used in the ImportExportPortlet;  the command-line tool does
     * not check permissions.
     */
    String EXPORT_ACTIVITY = "EXPORT_ENTITY";
    String DELETE_ACTIVITY = "DELETE_ENTITY";

    /*
      Permission types.  At present only 2, but that could change.
    */
    String PERMISSION_TYPE_GRANT = "GRANT";
    String PERMISSION_TYPE_DENY = "DENY";

    /*
     * Permission Owner Strings
     */

    /**
     * A String representing the uPortal framework, used, for example, for Permission.owner when the
     * framework grants a Permission.
     */
    String PORTAL_SYSTEM = "UP_SYSTEM";

    /** Represents the GaP subsystem as a permissions owner */
    String PORTAL_GROUPS = "UP_GROUPS";

    String PORTAL_PUBLISH = "UP_PORTLET_PUBLISH";

    String PORTAL_SUBSCRIBE = "UP_PORTLET_SUBSCRIBE";

    String PORTAL_USERS = "UP_USERS";

    String PORTAL_PERMISSIONS = "UP_PERMISSIONS";

    String ERROR_PORTLET = "UP_ERROR_CHAN";

    /*
      A String which, when concatentated with a portlet id, represents a portal
      portlet.  Used, for example, for Permission.target when the portal framework
      grants a Permission to perform some activity on a portlet.
      See PermissionHelper for a convenience method for correctly using this.
    */
    String PORTLET_PREFIX = "PORTLET_ID.";

    String ALL_PORTLET_TYPES = "ALL_PORTLET_TYPES";

    String ALL_PORTLETS_TARGET = "ALL_PORTLETS";

    String ALL_GROUPS_TARGET = "ALL_GROUPS";

    String ALL_CATEGORIES_TARGET = "ALL_CATEGORIES";

    String ALL_PERMISSIONS_ACTIVITY = "ALL_PERMISSIONS";

    String ALL_TARGET = "ALL";

    /** Non-owner-specific details target string (used by ERROR_PORTLET) */
    String DETAILS_TARGET = "DETAILS";

    /**
     * Gets the activity associated with this <code>IPermission</code>.
     *
     * @return String
     */
    String getActivity();

    /**
     * Gets that date that this <code>IPermission</code> should become effective on.
     *
     * @return date that this <code>IPermission</code> should become effective on
     */
    Date getEffective();

    /**
     * Gets the date that this <code>IPermission</code> should expire on.
     *
     * @return date that this <code>IPermission</code> should expire on
     */
    Date getExpires();

    /**
     * Returns the owner of this <code>IPermission</code>.
     *
     * @return owner of this <code>IPermission</code>
     */
    String getOwner();

    /**
     * Gets the target associated with this <code>IPermission</code>.
     *
     * @return target associated with this <code>IPermission</code>
     */
    String getTarget();

    /** Returns the <code>Permission</code> type. */
    String getType();

    /**
     * Sets the activity associated with this <code>IPermission</code>.
     *
     * @param activity String
     */
    void setActivity(String activity);

    /**
     * Sets the date that this <code>IPermission</code> should become effective on.
     *
     * @param effective java.util.Date
     */
    void setEffective(Date effective);

    /**
     * Sets the date that this <code>IPermission</code> should expire on.
     *
     * @param expires java.util.Date
     */
    void setExpires(Date expires);

    /**
     * Sets the target associated with this <code>IPermission</code>.
     *
     * @param target
     */
    void setTarget(String target);

    /**
     * Sets the <code>IPermission</code> type.
     *
     * @param type String
     */
    void setType(String type);

    /**
     * Returns a String representing the <code>IAuthorizationPrincipal</code> associated with this
     * <code>IPermission</code>.
     *
     * @return IAuthorizationPrincipal associated with this IPermission
     */
    String getPrincipal();

    /**
     * Sets the principal String representing the <code>IAuthorizationPrincipal</code> associated
     * with this <code>IPermission</code>.
     *
     * @param newPrincipal String
     */
    void setPrincipal(String newPrincipal);

}
