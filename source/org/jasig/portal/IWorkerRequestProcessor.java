package org.jasig.portal;

import java.io.IOException;
import javax.servlet.ServletException;

/**
 * An interface for worker request processors.
 * Note: workers are required for functionality that requires complete
 * control over the servlet output stream and, at the same time, requires
 * access to the internal structures.
 *
 * @version: $Revision$
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
