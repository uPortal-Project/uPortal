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

import org.apache.oro.text.perl.Perl5Util;


/**
 * A tester for matching the possibly multiple values of an attribute 
 * against a regular expression.  If any of the values matches the pattern, 
 * the tester returns true.
 * <p>
 * @author Dan Ellentuck
 * @version $Revision$
 */

public class RegexTester extends StringTester {
    protected Perl5Util regexMatcher = null;
    protected String pattern = null;
    protected char PATTERN_DELIMITER = '/';
    
public RegexTester(String attribute, String test) {
    super(attribute, test);
    initialize();
}
protected void initialize() {
    regexMatcher = new Perl5Util();
    pattern = PATTERN_DELIMITER + testValue + PATTERN_DELIMITER; 
}
public boolean test(String att) {
    boolean result = false;
    try
        { result = regexMatcher.match(pattern,att); }
    catch ( Throwable t ) { }  // Bad pattern?
    return result; 
}

}
