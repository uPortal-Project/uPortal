/*
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
package org.apereo.portal.api.permissions;

import org.junit.Assert;
import org.junit.Test;

public class TargetImplTest {
    TargetImpl target;

    @Test
    public void test() {
        target = new TargetImpl("key", "name");
        target.getKey();
    }

    @Test
    public void testGetKey() {
        target = new TargetImpl("key", "name");
        Assert.assertEquals("key", target.getKey());
    }

    @Test
    public void testGetName() {
        target = new TargetImpl("key", "name");
        Assert.assertEquals("name", target.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetKeyNull() {
        target = new TargetImpl(null, null);
        target.getKey();
    }
}
