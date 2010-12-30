/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlet.dao.jpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.portlet.rendering.IPortletRenderer;
import org.springframework.test.jpa.AbstractJpaTests;

/**
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision: 337 $
 */
public class JpaPortletDaoTest extends AbstractJpaTests {
    private EntityManager entityManager;
    private JpaPortletTypeDao jpaChannelTypeDao;
    private JpaPortletDefinitionDao jpaPortletDefinitionDao;
    private JpaPortletEntityDao jpaPortletEntityDao;
    
    public JpaPortletDaoTest() {
        this.setDependencyCheck(false);
    }
    
    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:jpaTestApplicationContext.xml"};
    }

    @PersistenceContext(unitName="uPortalPersistence")
    public void setEntityManager(EntityManager entityManager) {
        this.entityManager = entityManager;
    }
    public void setJpaPortletEntityDao(final JpaPortletEntityDao jpaPortletEntityDao) {
        this.jpaPortletEntityDao = jpaPortletEntityDao;
    }
    public void setJpaPortletDefinitionDao(final JpaPortletDefinitionDao dao) {
        this.jpaPortletDefinitionDao = dao;
    }
    public void setJpaChannelTypeDao(JpaPortletTypeDao jpaChannelTypeDao) {
        this.jpaChannelTypeDao = jpaChannelTypeDao;
    }

    
    public void testNoopOperations() throws Exception {
        final IPortletDefinitionId portletDefinitionId = new PortletDefinitionIdImpl(1);
        final IPortletDefinition nullPortDef1 = this.jpaPortletDefinitionDao.getPortletDefinition(portletDefinitionId);
        assertNull(nullPortDef1);
        
        final IPortletEntity nullPortEnt1 = this.jpaPortletEntityDao.getPortletEntity("chanSub1", 1);
        assertNull(nullPortEnt1);
        
        final Set<IPortletEntity> portEnts = this.jpaPortletEntityDao.getPortletEntities(new PortletDefinitionIdImpl(1));
        assertEquals(Collections.emptySet(), portEnts);
    }

    public void testAllDefinitionDaoMethods() throws Exception {
        final IPortletType channelType = this.jpaChannelTypeDao.createPortletType("BaseType", "foobar");
        this.checkPoint();
        
        //Create a definition
        final IPortletDefinition chanDef1 = this.jpaPortletDefinitionDao.createPortletDefinition(channelType, "fname1", IPortletRenderer.class.getName(), "Test Portlet 1", "Test Portlet 1 Title", "/context1", "portletName1", false);
        this.checkPoint();
        
        //Try all of the retrieval options
        final IPortletDefinition portDef1a = this.jpaPortletDefinitionDao.getPortletDefinition(chanDef1.getPortletDefinitionId());
        assertEquals(chanDef1, portDef1a);
        
        //Create a second definition with the same app/portlet
        IPortletDefinition chanDef2 = this.jpaPortletDefinitionDao.createPortletDefinition(channelType, "fname2", IPortletRenderer.class.getName(), "Test Portlet 2", "Test Portlet 2 Title", "/uPortal", "portletName2", true);
        this.checkPoint();
        
        
        // Add some preferences
        chanDef2 = this.jpaPortletDefinitionDao.getPortletDefinition(chanDef2.getPortletDefinitionId());
        final IPortletPreferences prefs2 = chanDef2.getPortletPreferences();
        final List<IPortletPreference> prefsList2 = prefs2.getPortletPreferences();
        prefsList2.add(new PortletPreferenceImpl("prefName1", false, "val1", "val2"));
        prefsList2.add(new PortletPreferenceImpl("prefName2", true, "val3", "val4"));
        
        this.jpaPortletDefinitionDao.updatePortletDefinition(chanDef2);
        this.checkPoint();
        
        
        // Check prefs, remove one and another
        final IPortletDefinition portDef3 = this.jpaPortletDefinitionDao.getPortletDefinition(chanDef2.getPortletDefinitionId());
        final IPortletPreferences prefs3 = portDef3.getPortletPreferences();
        final List<IPortletPreference> prefsList3 = prefs3.getPortletPreferences();
        
        final List<IPortletPreference> expectedPrefsList3 = new ArrayList<IPortletPreference>();
        expectedPrefsList3.add(new PortletPreferenceImpl("prefName1", false, "val1", "val2"));
        expectedPrefsList3.add(new PortletPreferenceImpl("prefName2", true, "val3", "val4"));
        
        assertEquals(expectedPrefsList3, prefsList3);
        
        
        prefsList3.remove(1);
        prefsList3.add(new PortletPreferenceImpl("prefName3", false, "val5", "val6"));
        
        this.jpaPortletDefinitionDao.updatePortletDefinition(portDef3);
        this.checkPoint();
        

        // Check prefs
        final IPortletDefinition portDef4 = this.jpaPortletDefinitionDao.getPortletDefinition(portDef3.getPortletDefinitionId());
        final IPortletPreferences prefs4 = portDef4.getPortletPreferences();
        final List<IPortletPreference> prefsList4 = prefs4.getPortletPreferences();
        
        final List<IPortletPreference> expectedPrefsList4 = new ArrayList<IPortletPreference>();
        expectedPrefsList4.add(new PortletPreferenceImpl("prefName1", false, "val1", "val2"));
        expectedPrefsList4.add(new PortletPreferenceImpl("prefName3", false, "val5", "val6"));
        
        assertEquals(expectedPrefsList4, prefsList4);
    }
    
    public void testAllEntityDaoMethods() throws Exception {
        final IPortletType channelType = this.jpaChannelTypeDao.createPortletType("BaseType", "foobar");
        this.checkPoint();
        
        //Create a definition
        IPortletDefinition chanDef1 = this.jpaPortletDefinitionDao.createPortletDefinition(channelType, "fname1", IPortletRenderer.class.getName(), "Test Portlet 1", "Test Portlet 1 Title", "/context1", "portletName1", false);
        this.checkPoint();
        
        IPortletEntity portEnt1 = this.jpaPortletEntityDao.createPortletEntity(chanDef1.getPortletDefinitionId(), "chanSub1", 1);
        this.checkPoint();
        
        
        final IPortletEntity portEnt1a = this.jpaPortletEntityDao.getPortletEntity(portEnt1.getPortletEntityId());
        assertEquals(portEnt1, portEnt1a);
        
        final IPortletEntity portEnt1b = this.jpaPortletEntityDao.getPortletEntity("chanSub1", 1);
        assertEquals(portEnt1, portEnt1b);
        
        final Set<IPortletEntity> portletEntities1 = this.jpaPortletEntityDao.getPortletEntities(chanDef1.getPortletDefinitionId());
        assertEquals(Collections.singleton(portEnt1), portletEntities1);
        
        final Set<IPortletEntity> portletEntitiesByUser = this.jpaPortletEntityDao.getPortletEntitiesForUser(1);
        assertEquals(Collections.singleton(portEnt1), portletEntitiesByUser);
        
        
        
        
        //Try deleting whole tree
        chanDef1 = this.jpaPortletDefinitionDao.getPortletDefinition(chanDef1.getPortletDefinitionId());
        chanDef1.getPortletPreferences().getPortletPreferences().add(new PortletPreferenceImpl("defpref1", false, "dpv1", "dpv2"));
        this.jpaPortletDefinitionDao.updatePortletDefinition(chanDef1);
        this.checkPoint();
        
        
        portEnt1 = this.jpaPortletEntityDao.getPortletEntity(portEnt1.getPortletEntityId());
        portEnt1.getPortletPreferences().getPortletPreferences().add(new PortletPreferenceImpl("entpref1", false, "epv1", "epv2"));
        this.jpaPortletEntityDao.updatePortletEntity(portEnt1);
        this.checkPoint();

        
        final IPortletDefinition chanDef2 = this.jpaPortletDefinitionDao.getPortletDefinition(chanDef1.getPortletDefinitionId());
        this.jpaPortletDefinitionDao.deletePortletDefinition(chanDef2);
        this.checkPoint();
        
        
        final Set<IPortletEntity> portletEntities2 = this.jpaPortletEntityDao.getPortletEntities(chanDef1.getPortletDefinitionId());
        assertEquals(Collections.emptySet(), portletEntities2);
    }

    private void checkPoint() {
        this.entityManager.flush();
        this.entityManager.clear();
    }
    
    public static class Util {
        public static <T> Set<T> unmodifiableSet(T... o) {
            return Collections.unmodifiableSet(new HashSet<T>(Arrays.asList(o)));
        }
    }
}
