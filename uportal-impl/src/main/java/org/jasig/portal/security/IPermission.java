/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
    Activity names for Permissions whose targets are Channels.
  */
  public String CHANNEL_MANAGER_ACTIVITY = "MANAGE";
  public String CHANNEL_PUBLISHER_ACTIVITY = "PUBLISH";
  public String CHANNEL_SUBSCRIBER_ACTIVITY = "SUBSCRIBE";
  
  /*
   * Channel subscribe permissions by lifecycle state 
   */
  public static final String CHANNEL_SUBSCRIBER_CREATED_ACTIVITY = "SUBSCRIBE_CREATED";
  public static final String CHANNEL_SUBSCRIBER_APPROVED_ACTIVITY = "SUBSCRIBE_APPROVED";
  public static final String CHANNEL_SUBSCRIBER_EXPIRED_ACTIVITY = "SUBSCRIBE_EXPIRED";
  
  /*
   * Channel render permissions by lifecycle state.  These permissions are not
   * actually used in the codebase yet and are included here for future planning 
   * purposes only.
   */
  public static final String CHANNEL_RENDERER_CREATED_ACTIVITY = "RENDER_CREATED";
  public static final String CHANNEL_RENDERER_APPROVED_ACTIVITY = "RENDER_APPROVED";
  public static final String CHANNEL_RENDERER_PUBLISHED_ACTIVITY = "RENDER_PUBLISHED";
  public static final String CHANNEL_RENDERER_EXPIRED_ACTIVITY = "RENDER_EXPIRED";
    
  /*
   * Channel management permissions by lifecycle state.
   */
  public static final String CHANNEL_MANAGER_CREATED_ACTIVITY = "MANAGE_CREATED";
  public static final String CHANNEL_MANAGER_APPROVED_ACTIVITY = "MANAGE_APPROVED";
  public static final String CHANNEL_MANAGER_EXPIRED_ACTIVITY = "MANAGE_EXPIRED";
    
  /*
    Permission types.  At present only 2, but that could change.
  */
  public String PERMISSION_TYPE_GRANT = "GRANT";
  public String PERMISSION_TYPE_DENY = "DENY";
  /*
    A String representing the uPortal framework, used, for example, for
    Permission.owner when the framework grants a Permission.
  */
  public String PORTAL_FRAMEWORK = "UP_FRAMEWORK";
  /*
    A String which, when concatentated with a channel id, represents a portal
    channel.  Used, for example, for Permission.target when the portal framework
    grants a Permission to perform some activity on a channel.
  */
  public String CHANNEL_PREFIX = "CHAN_ID.";    
  
  public String ALL_CHANNELS_TARGET = "ALL_CHANNELS";
  
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
