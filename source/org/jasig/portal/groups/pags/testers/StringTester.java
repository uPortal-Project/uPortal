/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.groups.pags.testers;

import org.jasig.portal.security.IPerson;

/**
 * Abstract class tests a possibly multi-valued attribute against
 * a test value.  
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */

public abstract class StringTester extends BaseAttributeTester {

public StringTester(String attribute, String test) {
    super(attribute, test);
}

public boolean test(IPerson person) {
    boolean result = false;
    Object[] atts = person.getAttributeValues(getAttributeName());
    if ( atts != null )
    {
        for (int i=0; i<atts.length && result == false; i++)
        { 
            String att = (String)atts[i];
            result = test(att); 
        }
    }
    return result;
}
public boolean test(String att) { return false; }

}
