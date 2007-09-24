/* Copyright 2003 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * An interface that a multithreaded privileged channel must implement.
 * @author Ken Weiner, kweiner@unicon.net
 * @version $Revision$
 * 
 * @deprecated Use the IChannel* interfaces instead or write a portlet. For more information see: 
 * http://www.ja-sig.org/wiki/display/UPC/Proposal+to+Deprecate+IMultithreaded+Interfaces
 */

public interface IMultithreadedPrivileged {
  
	/**
	 * Passes portal control structure to the channel.
	 * @see PortalControlStructures
	 */
	public void setPortalControlStructures(PortalControlStructures pcs, String uid) throws PortalException;

}



