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
package org.apereo.portal.persondir;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.IdentitySwapperManager;
import org.apereo.portal.url.IPortalRequestUtils;
import org.apereo.services.persondir.IPersonAttributeDaoFilter;
import org.apereo.services.persondir.IPersonAttributes;
import org.apereo.services.persondir.support.IUsernameAttributeProvider;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ImpersonationStatusPersonAttributeDaoTest {

    @InjectMocks
    private ImpersonationStatusPersonAttributeDao impersonationStatusPersonAttributeDao;

    @Mock private IPersonManager personManager;

    @Mock private IUsernameAttributeProvider usernameAttributeProvider;

    @Mock private IPerson person;

    @Mock private IdentitySwapperManager identitySwapperManager;

    @Mock private IPortalRequestUtils portalRequestUtils;

    @Mock private HttpServletRequest httpServletRequest;

    private Map<String, List<Object>> query = new HashMap<>();

    private String queryUid = "UserId";

    @Before
    public void setup() throws Exception {
        MockitoAnnotations.initMocks(this);
        when(portalRequestUtils.getCurrentPortalRequest()).thenReturn(httpServletRequest);
        when(person.getID()).thenReturn(1010);
        when(person.getFullName()).thenReturn("John Doe");
        when(person.getUserName()).thenReturn(queryUid);
        when(personManager.getPerson(httpServletRequest)).thenReturn(person);
        when(usernameAttributeProvider.getUsernameAttribute()).thenReturn("attrs");
        impersonationStatusPersonAttributeDao.setUsernameAttributeProvider(
                usernameAttributeProvider);
        when(usernameAttributeProvider.getUsernameFromQuery(query)).thenReturn(queryUid);
        when(identitySwapperManager.isImpersonating(httpServletRequest)).thenReturn(true);
    }

    @Test
    public void testGetAvailableQueryAttributes() {
        Set<String> attributes =
                impersonationStatusPersonAttributeDao.getAvailableQueryAttributes(
                        IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(attributes);
        String attribute = attributes.iterator().next();
        assertEquals(attribute, "attrs");
    }

    @Test
    public void testGetPeopleWithMultivaluedAttributes() {
        Set<IPersonAttributes> attributes =
                impersonationStatusPersonAttributeDao.getPeopleWithMultivaluedAttributes(
                        query, IPersonAttributeDaoFilter.alwaysChoose());
        assertNotNull(attributes);
        IPersonAttributes personAttributes = attributes.iterator().next();
        String uname = personAttributes.getName();
        Map<String, List<Object>> arg1 = personAttributes.getAttributes();
        assertNotNull(uname);
        assertEquals(uname, "UserId");
        // check user attributes map
        assertTrue(arg1.containsKey("impersonating"));
        List<Object> attrs = arg1.get("impersonating");
        assertEquals(attrs.get(0), "true");
    }
}
