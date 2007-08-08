/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
