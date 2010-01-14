/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal;


/**
 * An interface for worker request processors.
 * Note: workers are required for functionality that requires complete
 * control over the servlet output stream and, at the same time, requires
 * access to the internal structures.
 *
 * @version $Revision$
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @deprecated IChannel rendering code will be replaced with portlet specific rendering code in a future release
 */
@Deprecated
public interface IWorkerRequestProcessor {

    /**
     * Process a worker request.
     *
     * @param pcs a <code>PortalControlStructures</code> object
     * @exception PortalException if an error occurs
     */
    public void processWorkerDispatch(PortalControlStructures pcs) throws PortalException;

}
