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

package org.jasig.portal.utils;

import static org.junit.Assert.assertEquals;

import java.util.Formatter;

import org.jasig.portal.utils.TableFormatter.TableEntry;
import org.junit.Test;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class TableFormatterTest {
    @Test
    public void testTableFormatter() {
        
        final TableFormatter tableFormatter = new TableFormatter(
            new TableEntry<String>("Data Type", "-", "s"),
            new TableEntry<String>("Export", "-", "s"),
            new TableEntry<String>("Delete", "-", "s"));
        
        tableFormatter.addRow(
                new TableEntry<String>("portlet-definition", "-", "s"),
                new TableEntry<Boolean>(true, "-", "b"),
                new TableEntry<Boolean>(true, "-", "b"));
        
        tableFormatter.addRow(
                new TableEntry<String>("portlet-type", "-", "s"),
                new TableEntry<Boolean>(true, "-", "b"),
                new TableEntry<Boolean>(true, "-", "b"));
        
        tableFormatter.addRow(
                new TableEntry<String>("entity", "-", "s"),
                new TableEntry<Boolean>(true, "-", "b"),
                new TableEntry<Boolean>(false, "-", "b"));
        
        final StringBuilder result = new StringBuilder();
        tableFormatter.format(new Formatter(result));
        
        final String expected =
                " Data Type          | Export | Delete \n" +  
                "--------------------+--------+--------\n" +
                " portlet-definition | true   | true   \n" +
                " portlet-type       | true   | true   \n" +
                " entity             | true   | false  \n";

        assertEquals(expected, result.toString());
    }
}
