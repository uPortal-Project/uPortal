/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;

/**
 * This is an ancestor of the {@link IPrivilegedChannel} interface, allows for more general
 * handling of both regular and multithreaded ({@link IMultithreadedChannel}) privileged channels in the framework.
 * @author Peter Kharchenko {@link <a href="mailto:pkharchenko@interactivebusiness.com">pkharchenko@interactivebusiness.com</a>}
 * @version $Revision$
 * @see IPrivilegedChannel
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface IPrivileged
{
    /**
     * Passes portal control structure to the channel.
     * @see PortalControlStructures
     */
    public void setPortalControlStructures(PortalControlStructures pcs) throws PortalException;
}
