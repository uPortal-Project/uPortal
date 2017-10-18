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

public class OwnerImplTest {
    OwnerImpl owner;

    @Test
    public void test() {
        owner = new OwnerImpl("key", "name");
        owner.getKey();
    }

    @Test
    public void testGetKey() {
        owner = new OwnerImpl("key", "name");
        Assert.assertEquals("key", owner.getKey());
    }

    @Test
    public void testGetName() {
        owner = new OwnerImpl("key", "name");
        Assert.assertEquals("name", owner.getName());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetKeyNull() {
        owner = new OwnerImpl(null, null);
        owner.getKey();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetNameNull() {
        owner = new OwnerImpl(null, null);
        owner.getName();
    }
}
