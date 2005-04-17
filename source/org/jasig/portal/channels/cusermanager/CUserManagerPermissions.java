/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.channels.cusermanager;

import java.util.Properties;
import java.util.Enumeration;

import org.jasig.portal.IPermissible;
import org.jasig.portal.channels.permissionsmanager.RDBMPermissibleRegistry;

/**
 * @author smb1@cornell.edu
 * @version $Revision$ $Date$
 */
class CUserManagerPermissions implements IPermissible {

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