/**
 * Licensed to Jasig under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Jasig
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.tenants;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

/**
 * The following methods are excluded from unit testing.
 *
 * <p>1. createTenant 2. updateTenant
 */
public class TenantServiceTest {

    private static final Map<String, String> attributes;

    private static final Set<ITenant> tenants = new HashSet<>();

    @InjectMocks private TenantService tenantService;

    @Mock private ITenantDao tenantDao;

    @Mock private ITenant tenant;

    static {
        Map<String, String> attributeMap = new HashMap<>();
        attributeMap.put("tenant_attr_1", "value_1");
        attributeMap.put("tenant_attr_2", "value_2");
        attributeMap.put("tenant_attr_3", "value_3");
        attributes = Collections.unmodifiableMap(attributeMap);
    }

    @Before
    public void setup() throws Exception {
        tenants.clear();
        MockitoAnnotations.initMocks(this);
        when(tenant.getFname()).thenReturn("Tenant1");
        when(tenant.getName()).thenReturn("Tenant1 Tenant1");
        when(tenant.getAttributesMap()).thenReturn(attributes);
        tenants.add(tenant);
        when(tenantDao.getTenantByFName("Tenant1")).thenReturn(tenant);
        when(tenantDao.getTenantByName("Tenant1 Tenant1")).thenReturn(tenant);
        when(tenantDao.getAllTenants()).thenReturn(tenants);
    }

    @Test
    public void testGetTenantsList() {
        List<ITenant> tenants = tenantService.getTenantsList();
        assertEquals(tenants.size(), 1);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTenantByBlankFName() {
        tenantService.getTenantByFName("");
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateTenantNameSmall() {
        tenantService.validateName("5FF0");
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateTenantNameLarge() {
        tenantService.validateName("5FF09MU3KMR7UWMU3KREI37U0Q9KHUEG4MU3KMU3KWMU3KWMU3K");
    }

    @Test(expected = IllegalStateException.class)
    public void testValidateFName() {
        tenantService.validateFname("W5FF?0");
    }

    @Test
    public void testNotExistsTenantName() {
        boolean exists = tenantService.fnameExists("");
        assertFalse(exists);
        exists = tenantService.fnameExists("Tenant1");
        assertTrue(exists);
    }
}
