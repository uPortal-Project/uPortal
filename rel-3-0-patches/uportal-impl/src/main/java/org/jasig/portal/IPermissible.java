/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 *
 *  This interface represents the set of questions any channel or service ("Owner") must answer
 *  if it wants to delegate the responsibility of assigning and viewing permissions
 *  to the Permissions Manager channel.
 *
 *  Owners will be registered by the IPermissible classname that represents them.
 *  These classnames will be stored in a database "UP_PERMISSIBLE"
 *
 * @author  Alex vigdor av317@columbia.edu
 * @version $Revision$
 */
public interface IPermissible {

    /** Return a list of tokens representing all the activities this channel controls with permissions.
     * These tokens can be used by the channel to ascertain permissions at runtime after they have
     * been entered with the Permissions manager
     */    
    public String[] getActivityTokens();
    /** For a given activity token, return a human-readable string that describes the activity.
     * Used in rendering the Permissions Manager GUI.
     */    
    public String getActivityName(String token);
    /** Return an array of tokens representing all targets this channel controls with permissions.
     */    
    public String[] getTargetTokens();
    /** Return the human readable name of a target
     */    
    public String getTargetName(String token);
    /** Return the token used by this channel to represent itself as the owner of generated permissions.
     * Can be arbitrary, but must be unique - I've been using classnames.  This is also used by the channel
     * to request a PermissionManager from the AuthorizationService at runtime.
     */    
    public String getOwnerToken();
    /** Human-readable name of the owner - normally the Channel name.
     */    
    public String getOwnerName();
}

