/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout.alm;

import org.jasig.portal.PortalException;
import org.jasig.portal.layout.IUserLayout;

/**
 * An interface representing the user layout fragment.
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to reflect that it is part of Aggregated
 * Layouts.
 *
 * @author <a href="mailto:mvi@immagic.com">Michael Ivanov</a>
 * @version $Revision$
 */
public interface ILayoutFragment extends IUserLayout {

	/**
	 * Returns a fragment name
	 *
	 * @return an <code>String</code> fragment name
	 */
	public String getName();

	/**
		 * Returns a fragment description
		 *
		 * @return an <code>String</code> fragment description
		 */
	public String getDescription();
	
	/**
		 * Returns a fragment functional name
		 *
		 * @return an <code>String</code> fragment name
		 */
	public String getFunctionalName();
	
	/**
			 * Returns the fragment root Id.
			 *
			 * @return a <code>String</code> fragment root Id
			 * @exception PortalException if an error occurs
			 */
	public String getFragmentRootId() throws PortalException;
	

}
