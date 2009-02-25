/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.channels.permissionsmanager;

import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.IPermissible;
import org.jasig.portal.IServant;
import org.jasig.portal.security.IAuthorizationPrincipal;
import org.w3c.dom.Document;

/**
 * PermissionsSessionData
 *
 * A lightweight object used to store all values associated with a single channel
 * session of CPermissionsManager
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public class PermissionsSessionData {
  public ChannelStaticData staticData;
  public ChannelRuntimeData runtimeData;
  public String view;
  public Document XML;
  public IServant servant;
  public IAuthorizationPrincipal[] principals;
  public IPermissible[] owners;
  public long startRD;  // used for timing response generation
  public boolean gotActivities = false;
  public boolean gotTargets = false;
  public boolean gotOwners = false;
  public boolean isFinished = false;
  public boolean isAuthorized = false;

  public PermissionsSessionData() {
  }

}