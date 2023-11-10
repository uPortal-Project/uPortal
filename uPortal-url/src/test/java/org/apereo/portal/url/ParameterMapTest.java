/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.url;

import static junit.framework.Assert.assertTrue;
import static org.junit.Assert.assertArrayEquals;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.Test;

/** */
public class ParameterMapTest {
    @Test
    public void testParameterMapClone() {
        final ParameterMap m1 = new ParameterMap();
        m1.put("foo", new String[] {"b1", "b2"});
        m1.put("bar", new String[0]);

        final ParameterMap m2 = new ParameterMap(m1);

        assertTrue(m2.equals(m1));
    }

    /**
     * arranges a parameter map with test values
     *
     * @author snehit
     */
    public Map<String, List<String>> arrangeParameterMap() {
        Map<String, List<String>> paramMap = new LinkedHashMap<>();
        paramMap.put("param1", Arrays.asList("value1", "value2"));
        paramMap.put("param2", null);
        return paramMap;
    }

    /** @author snehitroda */
    @Test
    public void testConvertListMapWithValues() {
        // Arrange
        Map<String, List<String>> paramMap = arrangeParameterMap();

        // Act
        ParameterMap mapResult = ParameterMap.convertListMap(paramMap);

        // Assert
        assertArrayEquals(new String[] {"value1", "value2"}, mapResult.get("param1"));
    }

    /** @author snehitroda */
    @Test
    public void testConvertListMapWithNoValues() {
        // Arrange
        Map<String, List<String>> paramMap = arrangeParameterMap();

        // Act
        ParameterMap mapResult = ParameterMap.convertListMap(paramMap);

        // Assert
        assertArrayEquals(new String[0], mapResult.get("param2"));
    }
}
