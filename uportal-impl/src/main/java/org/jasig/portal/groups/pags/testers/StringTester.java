/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
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
