/* Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.portlet.dao.jpa;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.EntityManager;

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.springframework.test.jpa.AbstractJpaTests;

/**
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision: 337 $
 */
public class JpaPortletEntityDaoTest extends AbstractJpaTests {
    private JpaPortletEntityDao jpaPortletEntityDao;
    private JpaPortletDefinitionDao jpaPortletDefinitionDao;

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:jpaTestApplicationContext.xml"};
    }
    
    public void setJpaPortletEntityDao(final JpaPortletEntityDao jpaPortletEntityDao) {
        this.jpaPortletEntityDao = jpaPortletEntityDao;
    }
    
    public void setJpaPortletDefinitionDao(final JpaPortletDefinitionDao jpaPortletDefinitionDao) {
        this.jpaPortletDefinitionDao = jpaPortletDefinitionDao;
    }
    
    
    public void testNoopOperations() throws Exception {
        final IPortletEntity nullPortEnt1 = this.jpaPortletEntityDao.getPortletEntity("chanSub1", 1);
        assertNull(nullPortEnt1);
        
//        final Set<IPortletEntity> portEnts = this.jpaPortletEntityDao.getPortletEntities(new PortletDefinitionIdImpl("1"));
//        assertEquals(Collections.emptySet(), portEnts);
        
        final IPortletEntity portEnt = new PortletEntityImpl(new PortletDefinitionImpl(1), "chanSub1", 1);
        this.jpaPortletEntityDao.deletePortletEntity(portEnt);
    }

    public void testAllMethods() throws Exception {
        final IPortletDefinition portDef1 = this.jpaPortletDefinitionDao.createPortletDefinition(1);
        
        final IPortletEntity portEnt1 = this.jpaPortletEntityDao.createPortletEntity(portDef1.getPortletDefinitionId(), "chanSub1", 1);
        this.checkPoint();
        
        
        final IPortletEntity portEnt1a = this.jpaPortletEntityDao.getPortletEntity(portEnt1.getPortletEntityId());
        assertEquals(portEnt1, portEnt1a);
        
        final IPortletEntity portEnt1b = this.jpaPortletEntityDao.getPortletEntity("chanSub1", 1);
        assertEquals(portEnt1, portEnt1b);
        
        final IPortletDefinition portDef2 = this.jpaPortletDefinitionDao.getPortletDefinition(portDef1.getChannelDefinitionId());
        assertEquals(portDef1, portDef2);
        
        final Set<IPortletEntity> portletEntities1 = this.jpaPortletEntityDao.getPortletEntities(portDef1.getPortletDefinitionId());
        assertEquals(Collections.singleton(portEnt1), portletEntities1);
        
        final Set<IPortletEntity> portletEntitiesByUser = this.jpaPortletEntityDao.getPortletEntitiesForUser(1);
        assertEquals(Collections.singleton(portEnt1), portletEntitiesByUser);
        
        
        
        
        //Try deleting whole tree
        portDef1.getPortletPreferences().getPortletPreferences().add(new PortletPreferenceImpl("defpref1", false, "dpv1", "dpv2"));
        this.jpaPortletDefinitionDao.updatePortletDefinition(portDef1);
        this.checkPoint();
        
        
        portEnt1.getPortletPreferences().getPortletPreferences().add(new PortletPreferenceImpl("entpref1", false, "epv1", "epv2"));
        this.jpaPortletEntityDao.updatePortletEntity(portEnt1);
        this.checkPoint();

        
        final IPortletDefinition portDef4 = this.jpaPortletDefinitionDao.getPortletDefinition(1);
        this.jpaPortletDefinitionDao.deletePortletDefinition(portDef4);
        this.checkPoint();
        
        
        final Set<IPortletEntity> portletEntities2 = this.jpaPortletEntityDao.getPortletEntities(portDef1.getPortletDefinitionId());
        assertEquals(Collections.emptySet(), portletEntities2);
    }

    private void checkPoint() {
        final EntityManager entityManager = this.jpaPortletDefinitionDao.getEntityManager();
        entityManager.flush();
        entityManager.clear();
    }
    
    public static class Util {
        public static <T> Set<T> unmodifiableSet(T... o) {
            return Collections.unmodifiableSet(new HashSet<T>(Arrays.asList(o)));
        }
    }
}
