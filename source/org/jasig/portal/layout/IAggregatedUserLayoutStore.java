/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal.layout;

import java.util.Map;
import org.jasig.portal.IUserLayoutStore;
import org.jasig.portal.PortalException;
import org.jasig.portal.UserProfile;
import org.jasig.portal.security.IPerson;

/**
 * IAggregatedUserLayoutStore defines the base methods working with aggregated user layout store.
 *
 * @author Michael Ivanov
 * @version $Revision$
 */
public interface IAggregatedUserLayoutStore extends IUserLayoutStore {


   /**
     * Add the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param node a <code>ALNode</code> object specifying the node
     * @return a <code>ALNode</code> object specifying the node with the generated node ID
     * @exception PortalException if an error occurs
     */
    public ALNode addUserLayoutNode (IPerson person, UserProfile profile, ALNode node) throws PortalException;

    /**
     * Update the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param node a <code>ALNode</code> object specifying the node
     * @return a boolean result of this operation
     * @exception PortalException if an error occurs
     */
    public boolean updateUserLayoutNode (IPerson person, UserProfile profile, ALNode node) throws PortalException;

    /**
     * Update the new user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param node a <code>ALNode</code> node ID specifying the node
     * @return a boolean result of this operation
     * @exception PortalException if an error occurs
     */
    public boolean deleteUserLayoutNode (IPerson person, UserProfile profile, ALNode node) throws PortalException;

    /**
     * Gets the user layout node.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param nodeId a <code>String</code> node ID specifying the node
     * @return a <code>ALNode</code> object
     * @exception PortalException if an error occurs
     */

    public ALNode getUserLayoutNode (IPerson person, UserProfile profile, String nodeId) throws PortalException;

    /**
     * Returns the user layout internal representation.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @return a <code>IAggregatedLayout</code> object containing the internal representation of the user layout
     * @exception PortalException if an error occurs
     */

    public IAggregatedLayout getAggregatedLayout (IPerson person, UserProfile profile) throws Exception;

    /**
     * Persists user layout document.
     * @param person an <code>IPerson</code> object specifying the user
     * @param profile a user profile for which the layout is being stored
     * @param layout a <code>IAggregatedLayout</code> containing an aggregated user layout
     * @exception Exception if an error occurs
     */
    public void setAggregatedLayout (IPerson person, UserProfile  profile, IAggregatedLayout layout) throws Exception;


    /** 
     * Persists the fragment
     * @param person an <code>IPerson</code> object specifying the user
     * @param layoutImpl a <code>ILayoutFragment</code> object containing a fragment
     * @exception Exception if an error occurs
     */
    public void setFragment (IPerson person, ILayoutFragment layoutImpl ) throws Exception;

    /**
     * Returns the layout fragment as a user layout.
     * @param person an <code>IPerson</code> object specifying the user
     * @param fragmentId a fragment ID
     * @return a <code>ILayoutFragment</code> object containing the internal representation of the user fragment
     * @exception PortalException if an error occurs
     */
    public ILayoutFragment getFragment (IPerson person, String fragmentId) throws Exception;

     /**
     * Returns the fragment IDs/names which the user is an owner of
     * @param person an <code>IPerson</code> object specifying the user
     * @return a <code>Map</code> object containing the fragment IDs
     * @exception PortalException if an error occurs
     */
    public Map getFragments (IPerson person) throws Exception;

    /**
     * Returns the next fragment ID
     *
     * @return a <code>String</code> next fragment ID
     * @exception PortalException if an error occurs
     */
    public String getNextFragmentId() throws PortalException;

}
