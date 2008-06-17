/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.pags.testers;

import org.jasig.portal.groups.pags.IPersonTester;

/**
 * A tester for examining <code>IPerson</code> attributes.  
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */

public abstract class BaseAttributeTester implements IPersonTester {
    protected String attributeName;
    protected String testValue;

public BaseAttributeTester(String attribute, String test) {
    super();
    attributeName = attribute;
    testValue = test;
}
/**
 * @return String
 */
public String getAttributeName() {
    return attributeName;
}
/**
 * @return String
 */
public String getTestValue() {
    return testValue;
}
/**
 * return String
 */
public String asString(Object o) {
    String result = null;
    if ( o instanceof String )
        { result = (String)o; } 
    else
    {
        if ( o instanceof String[] ) 
        {
            String[] sa = (String[])o;
            if ( sa.length > 0 )
                { result = sa[0]; } 
        }
    }      
    return result;
}
public String toString() {
    return "Tester for " + getAttributeName() + " : " + getTestValue();
}

}
