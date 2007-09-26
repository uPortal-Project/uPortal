/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
