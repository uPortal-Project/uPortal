package org.apereo.portal.api.permissions;

import org.apereo.portal.api.Principal;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

public class AssignmentImplTest {
    AssignmentImpl assignment;

    @Test(expected = IllegalArgumentException.class)
    public void testOwnerNull() {
        assignment = new AssignmentImpl(null, null, null, null, true);
        assignment.getOwner();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testActivityNull() {
        assignment = new AssignmentImpl(null, null, null, null, true);
        assignment.getActivity();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPrincipalNull() {
        assignment = new AssignmentImpl(null, null, null, null, true);
        assignment.getPrincipal();
    }

    @Test(expected = IllegalArgumentException.class)
    public void testTargetNull() {
        assignment = new AssignmentImpl(null, null, null, null, true);
        Assert.assertNull(assignment.getTarget());
    }

    @Test
    public void testOwner() {
        Owner owner = Mockito.mock(Owner.class);
        Activity activity = Mockito.mock(Activity.class);
        Principal principal = Mockito.mock(Principal.class);
        Target target = Mockito.mock(Target.class);
        assignment = new AssignmentImpl(owner, activity, principal, target, true);
        Assert.assertNotNull(assignment.getOwner());
        Assert.assertNotNull(assignment.getActivity());
        Assert.assertNotNull(assignment.getPrincipal());
        Assert.assertNotNull(assignment.getTarget());
    }
}
