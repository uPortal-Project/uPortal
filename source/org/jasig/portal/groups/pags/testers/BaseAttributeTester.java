/**
 * Copyright (c) 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
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
