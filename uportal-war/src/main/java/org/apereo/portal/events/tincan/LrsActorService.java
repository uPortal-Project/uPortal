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
package org.apereo.portal.events.tincan;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.apereo.portal.events.tincan.om.LrsActor;
import org.jasig.services.persondir.IPersonAttributeDao;
import org.jasig.services.persondir.IPersonAttributes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * Create a LrsActor object based on the username.
 *
 */
@Service("lrsActorService")
public class LrsActorService implements ILrsActorService {
    private IPersonAttributeDao personAttributeDao;
    private Ehcache lrsActorCache;

    private String emailAttributeName = "mail";
    private String displayNameAttributeName = "displayName";

    @Autowired
    public void setPersonAttributeDao(IPersonAttributeDao personAttributeDao) {
        this.personAttributeDao = personAttributeDao;
    }

    @Autowired
    @Qualifier("org.apereo.portal.events.tincan.LrsActorCache")
    public void setLrsActorCache(Ehcache lrsActorCache) {
        this.lrsActorCache = lrsActorCache;
    }

    @Value("${org.apereo.portal.events.tincan.TinCanPortalEventAggregator.emailAttributeName:mail}")
    public void setEmailAttributeName(String emailAttributeName) {
        this.emailAttributeName = emailAttributeName;
    }

    @Value(
            "${org.apereo.portal.events.tincan.TinCanPortalEventAggregator.displayNameAttributeName:displayName}")
    public void setDisplayNameAttributeName(String displayNameAttributeName) {
        this.displayNameAttributeName = displayNameAttributeName;
    }

    @Override
    public LrsActor getLrsActor(String userName) {
        Element element = this.lrsActorCache.get(userName);
        if (element != null) {
            return (LrsActor) element.getObjectValue();
        }

        final String email;
        final String name;
        final IPersonAttributes person = personAttributeDao.getPerson(userName);
        if (person == null) {
            email = userName;
            name = userName + "@example.com";
        } else {
            email = getEmail(person);
            name = getName(person);
        }

        final LrsActor lrsActor = new LrsActor("mailto:" + email, name);
        this.lrsActorCache.put(new Element(userName, lrsActor));
        return lrsActor;
    }

    protected String getEmail(IPersonAttributes person) {
        final Object email = person.getAttributeValue(emailAttributeName);
        if (email != null) {
            return email.toString();
        }

        return person.getName() + "@example.com";
    }

    protected String getName(IPersonAttributes person) {
        final Object displayName = person.getAttributeValue(displayNameAttributeName);
        if (displayName != null) {
            return displayName.toString();
        }

        return person.getName();
    }
}
