package org.apereo.portal.portlets.layout.dlm.remoting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.apereo.portal.groups.IEntityGroup;
import org.apereo.portal.groups.IGroupMember;
import org.apereo.portal.layout.dlm.remoting.JsonEntityBean;
import org.apereo.portal.portlet.om.PortletCategory;
import org.apereo.portal.portlets.groupselector.EntityEnum;
import org.apereo.portal.security.IPermission;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class JsonEntityBeanTest {

    //Static test data
    String key = "test-key";
    String name = "test-name";
    String cId = "test-creator-id";
    String desc = "test-description";

    @Mock private IGroupMember groupMember;

    @Mock private IEntityGroup entityGroup;

    @Before
    public void setup() throws Exception {
        groupMember = Mockito.mock(IGroupMember.class);
        entityGroup = Mockito.mock(IEntityGroup.class);
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testConstructFromPortletCategory() {
        String id = "local.74";
        String name = "testName";
        String cId = "testCreatorId";
        String desc = "testDesc";

        PortletCategory pc = new PortletCategory(id);
        pc.setName(name);
        pc.setCreatorId(cId);
        pc.setDescription(desc);

        JsonEntityBean jeb = new JsonEntityBean(pc);

        assertEquals(EntityEnum.CATEGORY, jeb.getEntityType());
        assertEquals(id, jeb.getId());
        assertEquals(name, jeb.getName());
        assertEquals(cId, jeb.getCreatorId());
        assertEquals(desc, jeb.getDescription());
        assertEquals(id, jeb.getTargetString());
    }

    @Test
    public void testConstructFromGroupMemberWithPortlet() {
        String key = "test-key";
        Mockito.when(groupMember.getKey()).thenReturn(key);

        JsonEntityBean jeb = new JsonEntityBean(groupMember, EntityEnum.PORTLET);

        assertEquals(EntityEnum.PORTLET, jeb.getEntityType());
        assertEquals(key, jeb.getId());
        assertEquals(IPermission.PORTLET_PREFIX + key, jeb.getTargetString());
    }

    @Test
    public void testConstructFromGroupMemberWithPerson() {
        String key = "test-key";
        Mockito.when(groupMember.getKey()).thenReturn(key);

        JsonEntityBean jeb = new JsonEntityBean(groupMember, EntityEnum.PERSON);

        assertEquals(EntityEnum.PERSON, jeb.getEntityType());
        assertEquals(key, jeb.getId());
        assertEquals(key, jeb.getTargetString());
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromGroupMemberWithCategory() {
        String key = "test-key";
        Mockito.when(groupMember.getKey()).thenReturn(key);

        JsonEntityBean jeb = new JsonEntityBean(groupMember, EntityEnum.CATEGORY);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromGroupMemberWithGroup() {
        String key = "test-key";
        Mockito.when(groupMember.getKey()).thenReturn(key);

        JsonEntityBean jeb = new JsonEntityBean(groupMember, EntityEnum.GROUP);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromGroupMemberWithNullEntityEnum() {
        String key = "test-key";
        Mockito.when(groupMember.getKey()).thenReturn(key);

        JsonEntityBean jeb = new JsonEntityBean(groupMember, null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testConstructFromGroupOrEntityNull() {
        JsonEntityBean jeb = new JsonEntityBean(null, EntityEnum.PORTLET);
    }

    @Test
    public void testConstructFromEntityGroup() {
        JsonEntityBean jeb = buildBeanFromEntityGroup();

        assertEquals(EntityEnum.PORTLET, jeb.getEntityType());
        assertEquals(key, jeb.getId());
        assertEquals(name, jeb.getName());
        assertEquals(cId, jeb.getCreatorId());
        assertEquals(desc, jeb.getDescription());
        assertEquals(key, jeb.getTargetString());
    }

    @Test
    public void testGetTypeAndIdHash() {
        String key = "test one+two.three%four)five(*!@#six";
        String name = "test-name";
        String cId = "test-creator-id";
        String desc = "test-description";
        Mockito.when(entityGroup.getKey()).thenReturn(key);
        Mockito.when(entityGroup.getName()).thenReturn(name);
        Mockito.when(entityGroup.getCreatorID()).thenReturn(cId);
        Mockito.when(entityGroup.getDescription()).thenReturn(desc);

        JsonEntityBean jeb = new JsonEntityBean(entityGroup, EntityEnum.PORTLET);
        assertEquals(
                "portlet_test__one__two__three__four__five__________six", jeb.getTypeAndIdHash());
    }

    @Test(expected = java.lang.AssertionError.class)
    public void testGetTypeAndIdHashEntityEnumNull() {
        EntityEnum ee = null;
        JsonEntityBean jeb = buildBeanFromEntityGroup();
        jeb.setEntityType(ee);
        jeb.getTypeAndIdHash();
    }

    @Test(expected = java.lang.AssertionError.class)
    public void testGetTypeAndIdHashKeyNull() {
        String key = null;
        String name = "test-name";
        String cId = "test-creator-id";
        String desc = "test-description";
        Mockito.when(entityGroup.getKey()).thenReturn(key);
        Mockito.when(entityGroup.getName()).thenReturn(name);
        Mockito.when(entityGroup.getCreatorID()).thenReturn(cId);
        Mockito.when(entityGroup.getDescription()).thenReturn(desc);

        JsonEntityBean jeb = new JsonEntityBean(entityGroup, EntityEnum.PORTLET);
        jeb.getTypeAndIdHash();
    }

    @Test
    public void testHashCodeNull() {
        Mockito.when(groupMember.getKey()).thenReturn("");
        JsonEntityBean jeb1 = buildNullBean();
        JsonEntityBean jeb2 = buildNullBean();

        assertEquals(jeb1.hashCode(), jeb2.hashCode());
    }

    @Test
    public void testHashCode() {
        Mockito.when(groupMember.getKey()).thenReturn("");
        JsonEntityBean jeb1 = buildBeanFromEntityGroup();
        jeb1.setChildrenInitialized(true);
        JsonEntityBean jeb2 = buildBeanFromEntityGroup();
        jeb2.setChildrenInitialized(true);

        assertEquals(jeb1.hashCode(), jeb2.hashCode());
    }

    @Test
    public void testEqualsSelf() {
        JsonEntityBean jeb1 = buildNullBean();

        assertTrue(jeb1.equals(jeb1));
    }

    @Test
    public void testEqualsNull() {
        JsonEntityBean jeb1 = buildNullBean();

        assertFalse(jeb1.equals(null));
    }

    @Test
    public void testEqualsDifferentClasses() {
        JsonEntityBean jeb1 = buildNullBean();

        assertFalse(jeb1.equals(""));
    }

    @Test
    public void testEqualsDiffChildrenInit() {
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(false);

        assertFalse(jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsCreatorIdNullSource() {
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId(null);
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");

        assertFalse(jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsCreatorIdNullTarget() {
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId(null);

        assertFalse(jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsCreatorIdDiff() {
        String cid1 = "asdf";
        String cid2 = "hjkl";
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId(cid1);
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId(cid2);

        assertEquals(cid1.equals(cid2), jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsDescNullSource() {
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        jeb1.setDescription(null);
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");
        jeb2.setDescription("");

        assertFalse(jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsDescNullTarget() {
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        jeb1.setDescription("");
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");
        jeb2.setDescription(null);

        assertFalse(jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsDescDiff() {
        String d1 = "asdf";
        String d2 = "hjkl";
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        jeb1.setDescription(d1);
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");
        jeb2.setDescription(d2);

        assertEquals(d1.equals(d2), jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsEntityTypeNullSource() {
        EntityEnum ee = null;
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        jeb1.setDescription("");
        jeb1.setEntityType(ee);
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");
        jeb2.setDescription("");
        jeb2.setEntityType(EntityEnum.PORTLET);

        assertFalse(jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsEntityTypeNullTarget() {
        EntityEnum ee = null;
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        jeb1.setDescription("");
        jeb1.setEntityType(EntityEnum.PORTLET);
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");
        jeb2.setDescription("");
        jeb2.setEntityType(ee);

        assertFalse(jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsEntityTypeDiff() {
        EntityEnum val1 = EntityEnum.PORTLET;
        EntityEnum val2 = EntityEnum.CATEGORY;
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        jeb1.setDescription("");
        jeb1.setEntityType(val1);
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");
        jeb2.setDescription("");
        jeb2.setEntityType(val2);

        assertEquals(val1.equals(val2), jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsIdNullSource() {
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        jeb1.setDescription("");
        jeb1.setEntityType(EntityEnum.PORTLET);
        jeb1.setId(null);
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");
        jeb2.setDescription("");
        jeb2.setEntityType(EntityEnum.PORTLET);
        jeb2.setId("");

        assertFalse(jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsIdNullTarget() {
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        jeb1.setDescription("");
        jeb1.setEntityType(EntityEnum.PORTLET);
        jeb1.setId("");
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");
        jeb2.setDescription("");
        jeb2.setEntityType(EntityEnum.PORTLET);
        jeb2.setId(null);
        assertFalse(jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsIdDiff() {
        String val1 = "asdf";
        String val2 = "brtd";
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        jeb1.setDescription("");
        jeb1.setEntityType(EntityEnum.PORTLET);
        jeb1.setId(val1);
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");
        jeb2.setDescription("");
        jeb2.setEntityType(EntityEnum.PORTLET);
        jeb2.setId(val2);

        assertEquals(val1.equals(val2), jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsNameNullSource() {
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        jeb1.setDescription("");
        jeb1.setEntityType(EntityEnum.PORTLET);
        jeb1.setId("");
        jeb1.setName(null);
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");
        jeb2.setDescription("");
        jeb2.setEntityType(EntityEnum.PORTLET);
        jeb2.setId("");
        jeb2.setName("");
        assertFalse(jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsNameNullTarget() {
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        jeb1.setDescription("");
        jeb1.setEntityType(EntityEnum.PORTLET);
        jeb1.setId("");
        jeb1.setName("");
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");
        jeb2.setDescription("");
        jeb2.setEntityType(EntityEnum.PORTLET);
        jeb2.setId("");
        jeb2.setName(null);
        assertFalse(jeb1.equals(jeb2));
    }

    @Test
    public void testEqualsNameDiff() {
        String val1 = "asdf";
        String val2 = "brtd";
        JsonEntityBean jeb1 = buildNullBean();
        jeb1.setChildrenInitialized(true);
        jeb1.setCreatorId("");
        jeb1.setDescription("");
        jeb1.setEntityType(EntityEnum.PORTLET);
        jeb1.setId("");
        jeb1.setName(val1);
        JsonEntityBean jeb2 = buildNullBean();
        jeb2.setChildrenInitialized(true);
        jeb2.setCreatorId("");
        jeb2.setDescription("");
        jeb2.setEntityType(EntityEnum.PORTLET);
        jeb2.setId("");
        jeb2.setName(val2);

        assertEquals(val1.equals(val2), jeb1.equals(jeb2));
    }

    @Test
    public void testEquals() {
        JsonEntityBean jeb1 = buildBeanFromEntityGroup();
        JsonEntityBean jeb2 = buildBeanFromEntityGroup();

        assertTrue(jeb1.equals(jeb2));
    }

    @Test
    public void testCompareTo() {
        JsonEntityBean jeb1 = buildBeanFromEntityGroup();
        JsonEntityBean jeb2 = buildBeanFromEntityGroup();

        assertEquals(0, jeb1.compareTo(jeb2));
    }

    @Test
    public void testCompareToDiffName() {
        String val1 = "asdf";
        String val2 = "hjkl";
        JsonEntityBean jeb1 = buildBeanFromEntityGroup();
        jeb1.setName(val1);
        JsonEntityBean jeb2 = buildBeanFromEntityGroup();
        jeb2.setName(val2);
        assertEquals(val1.compareTo(val2), jeb1.compareTo(jeb2));
    }

    @Test
    public void testCompareToDiffEntityType() {
        EntityEnum val1 = EntityEnum.GROUP;
        EntityEnum val2 = EntityEnum.CATEGORY;
        JsonEntityBean jeb1 = buildBeanFromEntityGroup();
        jeb1.setEntityType(val1);
        JsonEntityBean jeb2 = buildBeanFromEntityGroup();
        jeb2.setEntityType(val2);
        assertEquals(val1.compareTo(val2), jeb1.compareTo(jeb2));
    }

    @Test
    public void testCompareToDiffCreatorId() {
        String val1 = "asdf";
        String val2 = "hjkl";
        JsonEntityBean jeb1 = buildBeanFromEntityGroup();
        jeb1.setId(val1);
        JsonEntityBean jeb2 = buildBeanFromEntityGroup();
        jeb2.setId(val2);
        assertEquals(val1.compareTo(val2), jeb1.compareTo(jeb2));
    }

    @Test
    public void testCompareToDiffDesc() {
        String val1 = "asdf";
        String val2 = "hjkl";
        JsonEntityBean jeb1 = buildBeanFromEntityGroup();
        jeb1.setDescription(val1);
        JsonEntityBean jeb2 = buildBeanFromEntityGroup();
        jeb2.setDescription(val2);
        assertEquals(val1.compareTo(val2), jeb1.compareTo(jeb2));
    }

    @Test
    public void testCompareToDiffId() {
        String val1 = "asdf";
        String val2 = "hjkl";
        JsonEntityBean jeb1 = buildBeanFromEntityGroup();
        jeb1.setId(val1);
        JsonEntityBean jeb2 = buildBeanFromEntityGroup();
        jeb2.setId(val2);
        assertEquals(val1.compareTo(val2), jeb1.compareTo(jeb2));
    }

    @Test
    public void testCompareToDiffPrincipalString() {
        String val1 = "asdf";
        String val2 = "hjkl";
        JsonEntityBean jeb1 = buildBeanFromEntityGroup();
        jeb1.setPrincipalString(val1);
        JsonEntityBean jeb2 = buildBeanFromEntityGroup();
        jeb2.setPrincipalString(val2);
        assertEquals(val1.compareTo(val2), jeb1.compareTo(jeb2));
    }

    @Test
    public void testToString() {
        JsonEntityBean jeb1 = buildBeanFromEntityGroup();
        assertEquals(
                "JsonEntityBean [entityType=portlet, id=test-key, name=test-name, creatorId=test-creator-id, description=test-description, principalString=null]",
                jeb1.toString());
    }

    private JsonEntityBean buildNullBean() {
        JsonEntityBean jeb = new JsonEntityBean(groupMember, EntityEnum.PORTLET);
        EntityEnum ee = null;
        jeb.setEntityType(ee);
        jeb.setChildrenInitialized(false);
        jeb.setCreatorId(null);
        jeb.setDescription(null);
        jeb.setId(null);
        jeb.setName(null);
        return jeb;
    }

    private JsonEntityBean buildBeanFromEntityGroup() {
        Mockito.when(entityGroup.getKey()).thenReturn(key);
        Mockito.when(entityGroup.getName()).thenReturn(name);
        Mockito.when(entityGroup.getCreatorID()).thenReturn(cId);
        Mockito.when(entityGroup.getDescription()).thenReturn(desc);

        return new JsonEntityBean(entityGroup, EntityEnum.PORTLET);
    }
}
