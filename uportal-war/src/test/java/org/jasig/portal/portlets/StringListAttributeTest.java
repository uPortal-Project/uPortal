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

package org.jasig.portal.portlets;

import static org.junit.Assert.*;
import org.junit.Test;

public class StringListAttributeTest {
    
    @Test
    public void testIsBlank_true() {
        
        final String[][] blankScenarios = new String[][] {
            // Zero entries
            new String[] {},
            // Null entry
            new String[] { null },
            // Zero-length entry
            new String[] { "" },
            // Whitespace entry
            new String[] { " " },
            // All of the above
            new String[] { null, "", " " }
        };
        
        for (String[] blank : blankScenarios) {
            StringListAttribute sla = new StringListAttribute(blank);
            assertTrue(
                "StringListAttribute should be blank for values:  " + blank.toString(), 
                sla.isBlank());
        }
        
    }

    @Test
    public void testIsBlank_false() {
        
        final String[][] nonBlankScenarios = new String[][] {
            // One good entry
            new String[] { "foobar" },
            // Multiple good entries
            new String[] { "Wynken", "Blynken", "Nod" }
        };
        
        for (String[] nonBlank : nonBlankScenarios) {
            StringListAttribute sla = new StringListAttribute(nonBlank);
            assertFalse(
                "StringListAttribute should NOT be blank for values:  " + nonBlank.toString(), 
                sla.isBlank());
        }
        
    }

}
