/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.pags.testers;

import org.jasig.portal.security.IPerson;

/**
 * Abstract class tests the possibly multiple values of an 
 * <code>IPerson</code> integer attribute. 
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */

public abstract class IntegerTester extends BaseAttributeTester {
    protected int testInteger = Integer.MIN_VALUE;

public IntegerTester(String attribute, String test) {
    super(attribute, test); 
    testInteger = Integer.parseInt(test);
}
public int getTestInteger() {
    return testInteger;
}
public boolean test(IPerson person) {
    boolean result = false;
    Object[] atts = person.getAttributeValues(getAttributeName());
    if ( atts != null )
    {
        for (int i=0; i<atts.length && result == false; i++)
        {
            try 
            {
                int integerAtt = Integer.parseInt((String)atts[i]);
                result = test( integerAtt );
            }
            catch (NumberFormatException nfe) {  } // result stays false
        }
    }
    return result;
}
public boolean test(int attributeValue) {return false;}

}