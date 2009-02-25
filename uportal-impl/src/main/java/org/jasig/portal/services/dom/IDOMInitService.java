/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
