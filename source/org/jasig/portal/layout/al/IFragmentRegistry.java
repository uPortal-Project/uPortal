/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al;

/**
 * An object responsible for supplying and maintaining fragments,
 * and permissions associated with these fragments.
 * 
 * @author Peter Kharchenko: pkharchenko at unicon.net
 */
public interface IFragmentRegistry {
	/**
	 * Obtain a layout fragment
	 * @param fragmentId fragment id
	 * @return fragment
	 */
	IFragment getFragment(IFragmentId fragmentId);
	
	/**
	 * Delete a fragment 
	 * 
	 * @param fragmentId
	 */
	void deleteFragment(IFragmentId fragmentId);
	
	/**
	 * Create a new, empty fragment
	 * 
	 * @return
	 */
	IFragment createNewFragment();
	
	/**
	 * Obtain a set of fragments that should be pushed to a user
	 * 
	 * @param userId
	 * @return an array of fragment ids
	 */
	IFragmentId[] getPushedFragments(String userId);
	
}
