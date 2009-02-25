/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups.pags.testers;

import org.jasig.portal.groups.pags.IPersonTester;
import org.jasig.portal.security.IPerson;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AlwaysTrueTester implements IPersonTester {
    
    public AlwaysTrueTester(String attribute, String test) {
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.groups.pags.IPersonTester#test(org.jasig.portal.security.IPerson)
     */
    public boolean test(IPerson person) {
        return true;
    }
}
