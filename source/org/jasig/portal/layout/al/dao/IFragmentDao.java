/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.layout.al.dao;

import org.jasig.portal.layout.al.IFragment;

/**
 * An interface defining fragment DAO operations.
 * 
 * @author Michael Ivanov: mvi at immagic.com
 * @version $Revision$
 */
public interface IFragmentDao {
    
	public void setFragment(IFragment fragment);
	
	public IFragment getFragment();
	
}
