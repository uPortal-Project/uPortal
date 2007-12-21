/* Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.portlet.dao.jpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.persistence.EntityManager;

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.springframework.test.jpa.AbstractJpaTests;

/**
 * @author Eric Dalquist <a href="mailto:eric.dalquist@doit.wisc.edu">eric.dalquist@doit.wisc.edu</a>
 * @version $Revision: 337 $
 */
public class JpaPortletDefinitionDaoTest extends AbstractJpaTests {
    private JpaPortletDefinitionDao jpaPortletDefinitionDao;

    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:jpaTestApplicationContext.xml"};
    }
    
    public void setJpaPortletDefinitionDao(final JpaPortletDefinitionDao dao) {
        this.jpaPortletDefinitionDao = dao;
    }

    
    public void testNoopOperations() throws Exception {
        final IPortletDefinition nullPortDef1 = this.jpaPortletDefinitionDao.getPortletDefinition(1);
        assertNull(nullPortDef1);
        
        final Set<IPortletDefinition> portDefs = this.jpaPortletDefinitionDao.getPortletDefinitions("appId", "portName");
        assertEquals(Collections.emptySet(), portDefs);
        
        final IPortletDefinition portDef = new PortletDefinitionImpl(1, "appId", "portName");
        this.jpaPortletDefinitionDao.deletePortletDefinition(portDef);
    }

    public void testAllMethods() throws Exception {
        //Create a definition
        final IPortletDefinition portDef1 = this.jpaPortletDefinitionDao.createPortletDefinition(1, "appId", "portName");
        this.checkPoint();
        
        
        //Try all of the retrieval options
        final IPortletDefinition portDef1a = this.jpaPortletDefinitionDao.getPortletDefinition(portDef1.getPortletDefinitionId());
        assertEquals(portDef1, portDef1a);
        
        final IPortletDefinition portDef1b = this.jpaPortletDefinitionDao.getPortletDefinition(1);
        assertEquals(portDef1, portDef1b);
        
        final Set<IPortletDefinition> portletDefinitions1 = this.jpaPortletDefinitionDao.getPortletDefinitions("appId", "portName");
        assertEquals(Collections.singleton(portDef1), portletDefinitions1);
        
        
        //Create a secod definition with the same app/portlet
        final IPortletDefinition portDef2 = this.jpaPortletDefinitionDao.createPortletDefinition(2, "appId", "portName");
        this.checkPoint();
        
        
        //Try Set based retrieval of both
        final Set<IPortletDefinition> portletDefinitions2 = this.jpaPortletDefinitionDao.getPortletDefinitions("appId", "portName");
        assertEquals(Util.unmodifiableSet(portDef1, portDef2), portletDefinitions2);
        
        
        //Remove the first definition
        this.jpaPortletDefinitionDao.deletePortletDefinition(portDef1);
        this.checkPoint();
        
        
        //Make sure the set now only has the second definition in it
        final Set<IPortletDefinition> portletDefinitions3 = this.jpaPortletDefinitionDao.getPortletDefinitions("appId", "portName");
        assertEquals(Collections.singleton(portDef2), portletDefinitions3);
        
        
        // Add some preferences
        final IPortletPreferences prefs2 = portDef2.getPortletPreferences();
        final List<IPortletPreference> prefsList2 = prefs2.getPortletPreferences();
        prefsList2.add(new PortletPreferenceImpl("prefName1", false, "val1", "val2"));
        prefsList2.add(new PortletPreferenceImpl("prefName2", true, "val3", "val4"));
        
        this.jpaPortletDefinitionDao.updatePortletDefinition(portDef2);
        this.checkPoint();
        
        
        // Check prefs, remove one and another
        final IPortletDefinition portDef3 = this.jpaPortletDefinitionDao.getPortletDefinition(portDef2.getPortletDefinitionId());
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
