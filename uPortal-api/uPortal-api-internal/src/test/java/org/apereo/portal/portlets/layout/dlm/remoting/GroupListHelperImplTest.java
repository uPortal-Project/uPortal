package org.apereo.portal.portlets.layout.dlm.remoting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import javax.naming.Name;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.IBasicEntity;
import org.apereo.portal.groups.GroupsException;
import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.groups.IIndividualGroupService;
import org.apereo.portal.layout.dlm.remoting.GroupListHelperImpl;
import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlets.groupselector.EntityEnum;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class GroupListHelperImplTest {

    @Mock private IEntityGroup entityGroup;

    @Mock private IGroupMember groupMember;

    @Before
    public void setup() throws Exception {
        entityGroup = Mockito.mock(IEntityGroup.class);
        groupMember = Mockito.mock(IGroupMember.class);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetEntityTypesForGroupTypeGroup() {
        GroupListHelperImpl helper = new GroupListHelperImpl();
        Set<String> res = helper.getEntityTypesForGroupType(EntityEnum.GROUP.name());
        assertEquals(2, res.size());
        assertTrue(res.contains(EntityEnum.GROUP.name()));
        assertTrue(res.contains(EntityEnum.PERSON.toString()));
    }

    @Test
    public void testGetEntityTypesForGroupTypeCategory() {
        GroupListHelperImpl helper = new GroupListHelperImpl();
        Set<String> res = helper.getEntityTypesForGroupType(EntityEnum.CATEGORY.name());
        assertEquals(2, res.size());
        assertTrue(res.contains(EntityEnum.CATEGORY.name()));
        assertTrue(res.contains(EntityEnum.PORTLET.toString()));
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEntityTypesForGroupTypePerson() {
        GroupListHelperImpl helper = new GroupListHelperImpl();
        Set<String> res = helper.getEntityTypesForGroupType(EntityEnum.PERSON.name());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEntityTypesForGroupTypePortlet() {
        GroupListHelperImpl helper = new GroupListHelperImpl();
        Set<String> res = helper.getEntityTypesForGroupType(EntityEnum.PORTLET.name());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEntityForPrincipalNull() {
        GroupListHelperImpl helper = new GroupListHelperImpl();
        helper.getEntityForPrincipal(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPrincipalForEntityNull() {
        GroupListHelperImpl helper = new GroupListHelperImpl();
        helper.getPrincipalForEntity(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testSearchEntityNull() {
        GroupListHelperImpl helper = new GroupListHelperImpl();
        helper.search(null, "asdf");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEntityNull() {
        GroupListHelperImpl helper = new GroupListHelperImpl();
        helper.getEntity(null, "asdf", true);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEntityTypeNull() {
        GroupListHelperImpl helper = new GroupListHelperImpl();
        helper.getEntityType(null);
    }

    @Test
    public void testGetEntityTypePortlet() {
        GroupListHelperImpl helper = new GroupListHelperImpl();
        IGroupMember mocked = manuallyMockGroupMember(IPortletDefinition.class);

        EntityEnum ee = helper.getEntityType(mocked);
        assertEquals("portlet", ee.toString());
    }

    @Test
    public void testGetEntityTypeCategory() {
        GroupListHelperImpl helper = new GroupListHelperImpl();
        MockedGroupMemberEntityGroup mocked =
                new MockedGroupMemberEntityGroup(IPortletDefinition.class);

        EntityEnum ee = helper.getEntityType(mocked);
        assertEquals("category", ee.toString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetPrincipalForEntityTypeIsNull() {
        GroupListHelperImpl helper = new GroupListHelperImpl();

        Mockito.when(entityGroup.getKey()).thenReturn("test-key");
        Mockito.when(entityGroup.getName()).thenReturn("test-name");
        Mockito.when(entityGroup.getCreatorID()).thenReturn("test-cid");
        Mockito.when(entityGroup.getDescription()).thenReturn("test-desc");

        JsonEntityBean jeb = new JsonEntityBean(entityGroup, EntityEnum.PORTLET);
        EntityEnum ee = null;
        jeb.setEntityType(ee);
        helper.getPrincipalForEntity(jeb);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLookupEntityNameNull() {
        GroupListHelperImpl helper = new GroupListHelperImpl();

        helper.lookupEntityName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testLookupEntityNameTypeIsNull() {
        GroupListHelperImpl helper = new GroupListHelperImpl();

        Mockito.when(entityGroup.getKey()).thenReturn("test-key");
        Mockito.when(entityGroup.getName()).thenReturn("test-name");
        Mockito.when(entityGroup.getCreatorID()).thenReturn("test-cid");
        Mockito.when(entityGroup.getDescription()).thenReturn("test-desc");

        JsonEntityBean jeb = new JsonEntityBean(entityGroup, EntityEnum.PORTLET);
        EntityEnum ee = null;
        jeb.setEntityType(ee);
        helper.lookupEntityName(jeb);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testPopulateChildrenBeanNull() {
        GroupListHelperImpl helper = new GroupListHelperImpl();

        helper.lookupEntityName(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testGetEntityByGroupMemberNull() {
        GroupListHelperImpl helper = new GroupListHelperImpl();

        helper.getEntity(null);
    }

    @Test
    public void testGetEntityBeansNull() {
        GroupListHelperImpl helper = new GroupListHelperImpl();

        assertEquals(Collections.EMPTY_LIST, helper.getEntityBeans(null));
    }

    @Test
    public void testGetEntityBeansEmpty() {
        GroupListHelperImpl helper = new GroupListHelperImpl();

        assertEquals(Collections.EMPTY_LIST, helper.getEntityBeans(new ArrayList<String>()));
    }

    private IGroupMember manuallyMockGroupMember(Class<? extends IBasicEntity> leafType) {
        return new IGroupMember() {
            @Override
            public Set<IEntityGroup> getAncestorGroups() throws GroupsException {
                return null;
            }

            @Override
            public Set<IEntityGroup> getParentGroups() throws GroupsException {
                return null;
            }

            @Override
            public String getKey() {
                return null;
            }

            @Override
            public Class<? extends IBasicEntity> getLeafType() {
                return leafType;
            }

            @Override
            public Class getType() {
                return null;
            }

            @Override
            public EntityIdentifier getUnderlyingEntityIdentifier() {
                return null;
            }

            @Override
            public boolean isDeepMemberOf(IEntityGroup group) throws GroupsException {
                return false;
            }

            @Override
            public boolean isGroup() {
                return false;
            }

            @Override
            public boolean isMemberOf(IEntityGroup group) throws GroupsException {
                return false;
            }

            @Override
            public IEntityGroup asGroup() {
                return null;
            }

            @Override
            public EntityIdentifier getEntityIdentifier() {
                return null;
            }
        };
    }

    private static final class MockedGroupMemberEntityGroup implements IGroupMember, IEntityGroup {
        private Class<? extends IBasicEntity> leafType;

        public MockedGroupMemberEntityGroup(Class<? extends IBasicEntity> leafType) {
            this.leafType = leafType;
        }

        @Override
        public boolean hasMembers() throws GroupsException {
            return false;
        }

        @Override
        public boolean contains(IGroupMember gm) throws GroupsException {
            return false;
        }

        @Override
        public boolean deepContains(IGroupMember gm) throws GroupsException {
            return false;
        }

        @Override
        public Set<IGroupMember> getChildren() throws GroupsException {
            return null;
        }

        @Override
        public Set<IGroupMember> getDescendants() throws GroupsException {
            return null;
        }

        @Override
        public void addChild(IGroupMember gm) throws GroupsException {}

        @Override
        public void delete() throws GroupsException {}

        @Override
        public String getCreatorID() {
            return null;
        }

        @Override
        public String getDescription() {
            return null;
        }

        @Override
        public String getLocalKey() {
            return null;
        }

        @Override
        public String getName() {
            return null;
        }

        @Override
        public Name getServiceName() {
            return null;
        }

        @Override
        public boolean isEditable() throws GroupsException {
            return false;
        }

        @Override
        public void removeChild(IGroupMember gm) throws GroupsException {}

        @Override
        public void setCreatorID(String userID) {}

        @Override
        public void setDescription(String name) {}

        @Override
        public void setName(String name) throws GroupsException {}

        @Override
        public void update() throws GroupsException {}

        @Override
        public void updateMembers() throws GroupsException {}

        @Override
        public void setLocalGroupService(IIndividualGroupService groupService)
                throws GroupsException {}

        @Override
        public Set<IEntityGroup> getAncestorGroups() throws GroupsException {
            return null;
        }

        @Override
        public Set<IEntityGroup> getParentGroups() throws GroupsException {
            return null;
        }

        @Override
        public String getKey() {
            return null;
        }

        @Override
        public Class<? extends IBasicEntity> getLeafType() {
            return leafType;
        }

        @Override
        public Class getType() {
            return null;
        }

        @Override
        public EntityIdentifier getUnderlyingEntityIdentifier() {
            return null;
        }

        @Override
        public boolean isDeepMemberOf(IEntityGroup group) throws GroupsException {
            return false;
        }

        @Override
        public boolean isGroup() {
            return false;
        }

        @Override
        public boolean isMemberOf(IEntityGroup group) throws GroupsException {
            return false;
        }

        @Override
        public IEntityGroup asGroup() {
            return null;
        }

        @Override
        public EntityIdentifier getEntityIdentifier() {
            return null;
        }
    }
}
