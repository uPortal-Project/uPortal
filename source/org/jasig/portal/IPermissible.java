/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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

