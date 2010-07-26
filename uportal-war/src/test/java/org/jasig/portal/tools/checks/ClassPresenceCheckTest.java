/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.tools.checks;

import junit.framework.TestCase;

/**
 * JUnit testcase for ClassPresenceCheck.
 * @version $Revision$ $Date$
 */
public class ClassPresenceCheckTest extends TestCase {

    /**
     * Test that checking for a present class succeeds.
     */
    public void testSuccess() {
        ClassPresenceCheck check = new ClassPresenceCheck("java.lang.Class");
        CheckResult result = check.doCheck();
        assertTrue(result.isSuccess());
    }
    
    /**
     * Test that checking for an absent class fails.
     */
    public void testFailure() {
        // check for the presence of a class we know will not exist.
        ClassPresenceCheck check = new ClassPresenceCheck("org.jasig.NoExist");
        CheckResult result = check.doCheck();
        assertFalse(result.isSuccess());
    }
}

