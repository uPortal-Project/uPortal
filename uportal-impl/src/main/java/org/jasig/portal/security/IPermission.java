/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
    Activity names for Permisisons whose targets are Channels.
  */
  public String CHANNEL_PUBLISHER_ACTIVITY = "PUBLISH";
  public String CHANNEL_SUBSCRIBER_ACTIVITY = "SUBSCRIBE";
  /*
    Permisison types.  At present only 2, but that could change.
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
  public String CHANNEL_PREFIX = "CHAN_ID.";    /**
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
