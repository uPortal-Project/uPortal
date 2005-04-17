/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

/**
 * An user-scoped registry responsible for supplying and maintaining fragments,
 * and permissions associated with these fragments.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 * @author Michael Ivanov: mvi at immagic.com
 */
public interface IFragmentRegistry {
	/**
	 * Obtain a layout fragment
	 * @param fragmentId fragment id
	 * @return fragment
	 */
	public IFragment getFragment(IFragmentId fragmentId);
	
	/**
	 * Delete a fragment 
	 * 
	 * @param fragmentId
	 */
	public void deleteFragment(IFragmentId fragmentId);
	
	/**
	 * Create a new, empty fragment
	 * 
	 * @return a new, empty fragment
	 */
	public IFragment createNewFragment();
	
	/**
	 * Obtain a set of fragments that should be pushed to a user
	 * 
	 * @return an array of fragment ids
	 */
	public IFragmentId[] getPushedFragments();
    
    /**
     * Obtain id of the user's main fragment (containing root node)
     * @return id of the user's main fragment
     */
    public IFragmentId getUserFragmentId();
	
}
