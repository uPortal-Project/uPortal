package org.apereo.portal.api.permissions;

import org.apereo.portal.permission.dao.IPermissionOwnerDao;
import org.apereo.portal.permission.target.IPermissionTargetProviderRegistry;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPermissionStore;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class ApiPermissionServiceImplTest {
    @InjectMocks ApiPermissionsService apiPermissionsService;

    @Mock private IAuthorizationService authorizationService;

    @Mock private IPermissionOwnerDao permissionOwnerDao;

    @Mock private IPermissionStore permissionStore;

    @Mock private IPermissionTargetProviderRegistry targetProviderRegistry;

    @Before
    public void setup() {
        apiPermissionsService = new ApiPermissionsService();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetAssignmentsForPerson() {
        Assert.assertNull(apiPermissionsService.getAssignmentsForPerson(null, true));
    }
}
