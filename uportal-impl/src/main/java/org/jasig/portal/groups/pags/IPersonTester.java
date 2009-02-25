/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups.pags;

import org.jasig.portal.security.IPerson;

 /**
 * A very basic interface for examining <code>IPersons</code>.  
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */

public interface IPersonTester {
    public boolean test(IPerson person);
}
