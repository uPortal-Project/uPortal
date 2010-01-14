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
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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