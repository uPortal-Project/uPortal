/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 * A lightwieht object used to store all values associated with a single channel
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