/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.pags.testers;

/**
 * Tests an <code>IPerson</code> attribute for String equality 
 * regardless of case and answers true if any of the possibly 
 * multiple values of the attribute equals the test value. 
 * Question: how to handle non-default locales?
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class StringEqualsIgnoreCaseTester extends StringTester {

public StringEqualsIgnoreCaseTester(String attribute, String test) {
    super(attribute, test);
}
public boolean test(String att) {
    return att.equalsIgnoreCase(testValue);
}

}
