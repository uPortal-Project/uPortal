/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.dom;

import org.jasig.portal.PortalException;

/**
 * This provides a DOM initialization service interface.
 * @author Nick Bolton, nbolton@unicon.net
 * @version $Revision$
 */
public interface IDOMInitService {
    /**
     * Executes an initialization procedure for a specific dom implementation.
     */
    public void initialize() throws PortalException;
}
