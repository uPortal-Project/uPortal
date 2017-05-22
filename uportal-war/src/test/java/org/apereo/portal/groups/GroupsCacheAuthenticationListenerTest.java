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
package org.apereo.portal.groups;

import java.util.Collections;
import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.PersonFactory;
import org.junit.Assert;
import org.junit.Test;

public class GroupsCacheAuthenticationListenerTest {

    @Test
    public void testUserAuthenticated() {

        final IPerson person = PersonFactory.createPerson();
        person.setAttribute(IPerson.USERNAME, "mock.person");

        final IEntityGroup group = new MockEntityGroup("mock.group", IPerson.class);

        final CacheManager cacheManager = CacheManager.getInstance();

        final Cache parentGroupsCache = new Cache("parentGroupsCache", 100, false, false, 0, 0);
        cacheManager.addCache(parentGroupsCache);
        parentGroupsCache.put(
                new Element(person.getEntityIdentifier(), Collections.singleton(group)));

        final Cache childrenCache = new Cache("childrenCache", 100, false, false, 0, 0);
        cacheManager.addCache(childrenCache);
        childrenCache.put(new Element(group.getUnderlyingEntityIdentifier(), new Object()));

        Assert.assertEquals(parentGroupsCache.getSize(), 1);
        Assert.assertEquals(childrenCache.getSize(), 1);

        final LocalGroupsCacheAuthenticationListener listener =
                new LocalGroupsCacheAuthenticationListener();
        listener.setParentGroupsCache(parentGroupsCache);
        listener.setChildrenCache(childrenCache);
        listener.userAuthenticated(person);

        Assert.assertEquals(parentGroupsCache.getSize(), 0);
        Assert.assertEquals(childrenCache.getSize(), 0);
    }
}
