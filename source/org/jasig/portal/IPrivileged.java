/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

/**
 * This is an ancestor of the {@link IPrivilegedChannel} interface, allows for more general
 * handling of both regular and multithreaded ({@link IMultithreadedChannel}) privileged channels in the framework.
 * @author Peter Kharchenko <a href="mailto:">pkharchenko@interactivebusiness.com</a>
 * @version $Revision$
 * @see IPrivilegedChannel
 */
public interface IPrivileged
{
    /**
     * Passes portal control structure to the channel.
     * @see PortalControlStructures
     */
    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException;
}
