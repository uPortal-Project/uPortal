/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.groups.pags.testers;


/**
 * Tests if any of the possibly multiple values of the attribute are GT
 * (greater than) the test value.  
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class IntegerGTTester extends IntegerTester {

public IntegerGTTester(String attribute, String test) {
    super(attribute, test); 
}
public boolean test(int attributeValue) {
    return attributeValue > testInteger;
}
}
