/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.groups.pags.testers

import org.junit.Test

class AdHocGroupTesterTest extends GroovyTestCase {

    @Test
    void testGroupsHash() {
        String example = "__Active_Students^Bad^Hackers_#"
        Set<String> includes = new HashSet<>()
        includes.add("Students")
        includes.add("Active")
        Set<String> excludes = new HashSet<>()
        excludes.add("Hackers")
        excludes.add("Bad")
        assertEquals(example, AdHocGroupTester.calcGroupsHash(includes, excludes))
    }
}
