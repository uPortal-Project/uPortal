/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;


/**
 * An interface for worker request processors.
 * Note: workers are required for functionality that requires complete
 * control over the servlet output stream and, at the same time, requires
 * access to the internal structures.
 *
 * @version $Revision$
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 */
public interface IWorkerRequestProcessor {

    /**
     * Process a worker request.
     *
     * @param pcs a <code>PortalControlStructures</code> object
     * @exception PortalException if an error occurs
     */
    public void processWorkerDispatch(PortalControlStructures pcs) throws PortalException;

}
