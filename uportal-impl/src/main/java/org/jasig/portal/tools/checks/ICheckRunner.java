/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.tools.checks;

import java.util.List;

/**
 * Interface for objects that execute IChecks and return Lists of CheckAndResult
 * objects.
 * @version $Revision$ $Date$
 * @since uPortal 2.5
 */
public interface ICheckRunner {
    
    /**
     * Execute some checks and return a List of CheckAndResult instances
     * representing the results.
     * @return a List of CheckAndResult instances.
     */
    public List<CheckAndResult> doChecks();
}
