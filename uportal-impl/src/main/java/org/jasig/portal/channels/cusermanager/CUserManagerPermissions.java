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

package org.jasig.portal.channels.cusermanager;

import java.util.Properties;
import java.util.Enumeration;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.IPermissible;
import org.jasig.portal.channels.permissionsmanager.RDBMPermissibleRegistry;

/**
 * @author smb1@cornell.edu
 * @version $Revision$ $Date$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
class CUserManagerPermissions implements IPermissible {
	
	protected final Log log = LogFactory.getLog(getClass());
	

    /**
     *  Values for use by the permissions manager
     */
    public static String[] activityTokens;

    /**
     *  Description of the Field
     */
    public static String[] activityNames;

    protected static final Properties activities = new Properties();

    static {

        activities.put( Constants.PERMISSION_MNGRRIGHT,
                             Constants.PERMISSION_MNGRRIGHTDESC );
        activities.put( Constants.PERMISSION_PWDCHNGRIGHT,
                             Constants.PERMISSION_PWDCHNGRIGHTDESC );
    }
    
    CUserManagerPermissions() {
        RDBMPermissibleRegistry.registerPermissible( Constants.PERMISSION_OWNERTOKEN );
    }// static

    /**
     * Return a list of tokens representing all the activities this channel controls with permissions.
     * These tokens can be used by the channel to ascertain permissions at runtime after they have
     * been entered with the Permissions manager
     */
    public String[] getActivityTokens() {

      if( activityTokens == null ){

        activityTokens = new String[ activities.size() ];

        int i = 0;
        Enumeration E = activities.keys();
        while( E.hasMoreElements())
          activityTokens[ i++ ] = (String)E.nextElement();

      }// if

      return activityTokens;
    }// getActivityTokens

    /**
     * For a given activity token, return a human-readable string that describes the activity.
     * Used in rendering the Permissions Manager GUI.
     */
    public String getActivityName(String token) {
      return activities.getProperty( token );
    }// getActivityName

    /**
     * Return an array of tokens representing all targets this channel controls with permissions.
     */
    public String[] getTargetTokens() {
      return new String[] { Constants.PERMISSION_OWNERTARGET };
    }// getTargetTokens

    /**
     * Return the human readable name of a target
     */
    public String getTargetName(String token) {
      return Constants.PERMISSION_OWNERTARGET;
    }// getTargetName

    /**
     * Return the token used by this channel to represent itself as the owner of generated permissions.
     * Can be arbitrary, but must be unique - I've been using classnames.  This is also used by the channel
     * to request a PermissionManager from the AuthorizationService at runtime.
     */
    public String getOwnerToken() {
      return Constants.PERMISSION_OWNERTOKEN;
    }// getOwnerToken

    /**
     * Human-readable name of the owner - normally the Channel name.
     */
    public String getOwnerName() {
      return Constants.PERMISSION_OWNERTARGET; //Constants.PERMISSION_OWNERNAME;
    }// getOwnerName


}// eoc