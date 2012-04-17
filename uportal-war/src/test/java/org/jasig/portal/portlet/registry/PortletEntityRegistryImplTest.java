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

package org.jasig.portal.portlet.registry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.concurrent.Callable;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.servlet.http.HttpServletRequest;

import net.sf.ehcache.Ehcache;

import org.jasig.portal.IUserPreferencesManager;
import org.jasig.portal.layout.IUserLayoutManager;
import org.jasig.portal.layout.node.IUserLayoutChannelDescription;
import org.jasig.portal.layout.node.IUserLayoutNodeDescription.LayoutNodeType;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.dao.IPortletEntityDao;
import org.jasig.portal.portlet.dao.IPortletTypeDao;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.test.BaseJpaDaoTest;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.user.IUserInstance;
import org.jasig.portal.user.IUserInstanceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaPortalTestApplicationContext.xml")
public class PortletEntityRegistryImplTest extends BaseJpaDaoTest {
    @Autowired
    private IPortletTypeDao jpaPortletTypeDao;
    @Autowired
    private IPortletDefinitionDao jpaPortletDefinitionDao;
    @Autowired
    private IPortletEntityDao jpaPortletEntityDao;
    
    @InjectMocks private PortletEntityRegistryImpl portletEntityRegistry = new PortletEntityRegistryImpl() {
		@Override
		protected IPortletDefinition getPortletDefinition(HttpServletRequest request, String portletDefinitionIdStr) {
			//Can't unit test authZ code so this is a stand in
			return jpaPortletDefinitionDao.getPortletDefinition(portletDefinitionIdStr);
		}

		@Override
		protected IPortletDefinition getPortletDefinition(HttpServletRequest request, IPortletDefinitionId portletDefinitionId) {
			//Can't unit test authZ code so this is a stand in
			return jpaPortletDefinitionDao.getPortletDefinition(portletDefinitionId);
		}

		@Override
		protected IPortletDefinition getPortletDefinition(HttpServletRequest request, IUserInstance userInstance, String portletDefinitionIdStr) {
			//Can't unit test authZ code so this is a stand in
			return jpaPortletDefinitionDao.getPortletDefinition(portletDefinitionIdStr);
		}

		@Override
		protected IPortletDefinition getPortletDefinition(IUserInstance userInstance, IPortletDefinitionId portletDefinitionId) {
			//Can't unit test authZ code so this is a stand in
			return jpaPortletDefinitionDao.getPortletDefinition(portletDefinitionId);
		}
    	
    }; 
    @Mock private IPortalRequestUtils portalRequestUtils;
    @Mock private IPortletDefinitionRegistry portletDefinitionRegistry;
    @Mock private Ehcache entityIdParseCache;
    @Mock private IUserInstanceManager userInstanceManager;
    @Mock private IUserInstance userInstance;
    @Mock private IUserPreferencesManager preferencesManager;
    @Mock private IUserLayoutManager userLayoutManager;
    @Mock private IUserLayoutChannelDescription node;
    @Mock private IPerson person;
    
    @PersistenceContext(unitName = "uPortalPersistence")
    private EntityManager entityManager;
    
    @Override
    protected EntityManager getEntityManager() {
        return this.entityManager;
    }

    @Before
    public void onSetUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        
        portletEntityRegistry.setPortletEntityDao(this.jpaPortletEntityDao);
        
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                for (final IPortletDefinition portletDefinition : jpaPortletDefinitionDao.getPortletDefinitions()) {
                    jpaPortletDefinitionDao.deletePortletDefinition(portletDefinition);
                }
                
                for (final IPortletType portletType : jpaPortletTypeDao.getPortletTypes()) {
                    jpaPortletTypeDao.deletePortletType(portletType);
                }
                
                return null;
            }
        });
    }

    protected IPortletDefinitionId createDefaultPorltetDefinition() {
        return this.execute(new Callable<IPortletDefinitionId>() {
            @Override
            public IPortletDefinitionId call() throws Exception {
                final IPortletType channelType = jpaPortletTypeDao.createPortletType("BaseType", "foobar");
                
                //Create a definition
                final IPortletDefinition portletDef = jpaPortletDefinitionDao.createPortletDefinition(channelType, "fname1", "Test Portlet 1", "Test Portlet 1 Title", "/context1", "portletName1", false);
                final IPortletDefinitionId portletDefinitionId = portletDef.getPortletDefinitionId();

                when(portletDefinitionRegistry.getPortletDefinition(portletDefinitionId)).thenReturn(portletDef);
                when(portletDefinitionRegistry.getPortletDefinition(portletDefinitionId.getStringId())).thenReturn(portletDef);

                return portletDefinitionId;
            }
        });
    }
    
    //persistent with prefs & not in db - create new & update
    @Test
    public void testPersistentWithPrefsNotInDb() throws Throwable {
        final IPortletDefinitionId portDefId1 = this.createDefaultPorltetDefinition();
        final String nodeId = "u1l1n1";
        
        //Mock setup
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(portalRequestUtils.getOriginalPortletOrPortalRequest(request)).thenReturn(request);
        
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(userInstance.getPerson()).thenReturn(person);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        
        when(userLayoutManager.getNode(nodeId)).thenReturn(node);
        when(node.getType()).thenReturn(LayoutNodeType.PORTLET);
        when(node.getChannelPublishId()).thenReturn(portDefId1.getStringId());
        
        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //T1 - Create the entity
                final IPortletEntity portletEntity = portletEntityRegistry.getOrCreatePortletEntity(request, portDefId1, nodeId, 12);
                assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());
                
                return portletEntity.getPortletEntityId();
            }
        });
        
        /*
         * T1 create entity
         * T1 add preference, making persistent
         * T2 delete preference, making interim
         * T1 add preference 2 to persistent, stays persistent
         */


        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                
                //Add a preference
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref", false, "value");
                preferences.add(portletPreference);
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(request, portletEntity);
                
                return null;
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //T1 - Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                assertEquals(1, preferences.size());
                
                //T2 - get the entity and add preferences
                final IPortletEntityId localPortletEntityId = executeInThread("T2.1", new Callable<IPortletEntityId>() {
                    @Override
                    public IPortletEntityId call() throws Exception {
                        //T2 - Get entity
                        final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId.getStringId());
                        assertEquals(portletEntity, portletEntity);
        
                        //T2 - add preference
                        final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                        preferences.clear();
                        
                        //T2 - Store the entity
                        portletEntityRegistry.storePortletEntity(request, portletEntity);
                        
                        return portletEntity.getPortletEntityId();
                    }
                });

                //T2 - verify entity was made persistent
                executeInThread("T2.2", new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        
                        //T2 - Verify it was converted from persistent to interim
                        final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, localPortletEntityId);
                        assertNotNull(portletEntity);
                        assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());
                        final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                        assertEquals(0, preferences.size());
                        
                        return null;
                    }
                });
        

                //T1 - add preference 2
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref2", false, "value");
                preferences.add(portletPreference);
        
                //T1 - Store the entity
                portletEntityRegistry.storePortletEntity(request, portletEntity);
                
                return null;
            }
        });
        
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //T1 - Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                assertEquals(2, preferences.size());
                
                return null;
            }
        });
    }
    
    //persistent with no prefs & not in db - noop
    @Test
    public void testPersistentNoPrefsNotInDb() throws Throwable {
        final IPortletDefinitionId portDefId1 = this.createDefaultPorltetDefinition();
        final String nodeId = "u1l1n1";
        
        //Mock setup
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(portalRequestUtils.getOriginalPortletOrPortalRequest(request)).thenReturn(request);
        
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(userInstance.getPerson()).thenReturn(person);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        
        when(userLayoutManager.getNode(nodeId)).thenReturn(node);
        when(node.getType()).thenReturn(LayoutNodeType.PORTLET);
        when(node.getChannelPublishId()).thenReturn(portDefId1.getStringId());

        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //T1 - Create the entity
                final IPortletEntity portletEntity = portletEntityRegistry.getOrCreatePortletEntity(request, portDefId1, nodeId, 12);;
                assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());
                
                return portletEntity.getPortletEntityId();
            }
        });


        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                
                //Add a preference
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref", false, "value");
                preferences.add(portletPreference);
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(request, portletEntity);
                
                return null;
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //T1 - Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                assertEquals(1, preferences.size());
                
                //T2 - get the entity and add preferences
                final IPortletEntityId localPortletEntityId = executeInThread("T2.1", new Callable<IPortletEntityId>() {
                    @Override
                    public IPortletEntityId call() throws Exception {
                        //T2 - Get entity
                        final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId.getStringId());
                        assertEquals(portletEntity, portletEntity);
        
                        //T2 - remove preferences
                        final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                        preferences.clear();
                        
                        //T2 - Store the entity
                        portletEntityRegistry.storePortletEntity(request, portletEntity);
                        
                        return portletEntity.getPortletEntityId();
                    }
                });

                //T2 - verify entity was made persistent
                executeInThread("T2.2", new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        
                        //T2 - Verify it was converted from persistent to interim
                        IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, localPortletEntityId);
                        assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());
                        List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                        assertEquals(0, preferences.size());
                        
                        return null;
                    }
                });
        

                //T1 - remove all preferences
                preferences.clear();
        
                //T1 - Store the entity
                portletEntityRegistry.storePortletEntity(request, portletEntity);
                
                return null;
            }
        });
        
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //T1 - Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                assertEquals(0, preferences.size());
                
                return null;
            }
        });
    }
    
    //interim with no prefs & in db - delete db version
    @Test
    public void testInterimNoPrefsAlreadyPersistent() throws Throwable {
        final IPortletDefinitionId portDefId1 = this.createDefaultPorltetDefinition();
        final String nodeId = "u1l1n1";
        
        //Mock setup
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(portalRequestUtils.getOriginalPortletOrPortalRequest(request)).thenReturn(request);
        
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(userInstance.getPerson()).thenReturn(person);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        
        when(userLayoutManager.getNode(nodeId)).thenReturn(node);
        when(node.getType()).thenReturn(LayoutNodeType.PORTLET);
        when(node.getChannelPublishId()).thenReturn(portDefId1.getStringId());

        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //T1 - Create the entity
                final IPortletEntity portletEntity = portletEntityRegistry.getOrCreatePortletEntity(request, portDefId1, nodeId, 12);;
                assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());
                
                //T2 - get the entity and add preferences
                final IPortletEntityId portletEntityId = executeInThread("T2.1", new Callable<IPortletEntityId>() {
                    @Override
                    public IPortletEntityId call() throws Exception {
                        //T2 - Get entity
                        final IPortletEntity localPortletEntity = portletEntityRegistry.getPortletEntity(request, portletEntity.getPortletEntityId().getStringId());
                        assertEquals(portletEntity, localPortletEntity);
        
                        //T2 - Add a preference
                        final List<IPortletPreference> preferences = localPortletEntity.getPortletPreferences();
                        final IPortletPreference portletPreference = new PortletPreferenceImpl("pref2", false, "value");
                        preferences.add(portletPreference);
                        
                        //T2 - Store the entity
                        portletEntityRegistry.storePortletEntity(request, localPortletEntity);
                        
                        return localPortletEntity.getPortletEntityId();
                    }
                });

                //T2 - verify entity was made persistent
                executeInThread("T2.2", new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        
                        //T2 - Verify it was converted from interim to persistent
                        IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                        assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                        List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                        assertEquals(1, preferences.size());
                        
                        return null;
                    }
                });
        
                //T1 - clear preferences
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                preferences.clear();
        
                //T1 - Store the entity
                portletEntityRegistry.storePortletEntity(request, portletEntity);
                
                return portletEntity.getPortletEntityId();
            }
        });
        
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //T1 - Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                assertEquals(0, preferences.size());
                
                return null;
            }
        });
    }
    
    //interim with prefs & in db - get db version & update
    @Test
    public void testInterimAddingPrefsAlreadyPersistent() throws Throwable {
        final IPortletDefinitionId portDefId1 = this.createDefaultPorltetDefinition();
        final String nodeId = "u1l1n1";
        
        //Mock setup
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(portalRequestUtils.getOriginalPortletOrPortalRequest(request)).thenReturn(request);
        
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(userInstance.getPerson()).thenReturn(person);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        
        when(userLayoutManager.getNode(nodeId)).thenReturn(node);
        when(node.getType()).thenReturn(LayoutNodeType.PORTLET);
        when(node.getChannelPublishId()).thenReturn(portDefId1.getStringId());

        /*
         * T1 - create ientity
         * T2 - get ientity
         * T2 - add pref and store ientity converting it to pentity
         * T1 - add pref to ientity and store
         */

        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //T1 - Create the entity
                final IPortletEntity portletEntity = portletEntityRegistry.getOrCreatePortletEntity(request, portDefId1, nodeId, 12);;
                assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());
                
                //T2 - get the entity and add preferences
                final IPortletEntityId portletEntityId = executeInThread("T2.1", new Callable<IPortletEntityId>() {
                    @Override
                    public IPortletEntityId call() throws Exception {
                        //T2 - Get entity
                        final IPortletEntity localPortletEntity = portletEntityRegistry.getPortletEntity(request, portletEntity.getPortletEntityId().getStringId());
                        assertEquals(portletEntity, localPortletEntity);
        
                        //T2 - Add a preference
                        final List<IPortletPreference> preferences = localPortletEntity.getPortletPreferences();
                        final IPortletPreference portletPreference = new PortletPreferenceImpl("pref2", false, "value");
                        preferences.add(portletPreference);
                        
                        //T2 - Store the entity
                        portletEntityRegistry.storePortletEntity(request, localPortletEntity);
                        
                        return localPortletEntity.getPortletEntityId();
                    }
                });

                //T2 - verify entity was made persistent
                executeInThread("T2.2", new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        
                        //T2 - Verify it was converted from interim to persistent
                        IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                        assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                        List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                        assertEquals(1, preferences.size());
                        
                        return null;
                    }
                });
        
                //T1 - Add a preference
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref1", false, "value");
                preferences.add(portletPreference);
        
                //T1 - Store the entity
                portletEntityRegistry.storePortletEntity(request, portletEntity);
                
                return portletEntity.getPortletEntityId();
            }
        });
        
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //T1 - Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                assertEquals(1, preferences.size());
                
                return null;
            }
        });
    }
    
    //persistent with no prefs & in db - delete & create interim
    @Test
    public void testPersistentRemovePrefs() throws Exception {
        final IPortletDefinitionId portletDefId = this.createDefaultPorltetDefinition();
        final String nodeId = "u1l1n1";
        
        //Mock setup
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(portalRequestUtils.getOriginalPortletOrPortalRequest(request)).thenReturn(request);
        
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(userInstance.getPerson()).thenReturn(person);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        
        when(userLayoutManager.getNode(nodeId)).thenReturn(node);
        when(node.getType()).thenReturn(LayoutNodeType.PORTLET);
        when(node.getChannelPublishId()).thenReturn(portletDefId.getStringId());


        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //Create the entity
                IPortletEntity portletEntity = portletEntityRegistry.getOrCreatePortletEntity(request, portletDefId, nodeId, 12);
                assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());

                return portletEntity.getPortletEntityId();
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                
                //Add a preference
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref", false, "value");
                preferences.add(portletPreference);
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(request, portletEntity);
                
                return null;
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                assertEquals(1, preferences.size());

                //remove all preferences
                preferences.clear();
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(request, portletEntity);
                
                return null;
            }
        });


        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //Verify it switched from persistent to interim
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                assertEquals(0, preferences.size());
                
                return null;
            }
        });
    }

    //persistent with prefs & in db - update
    @Test
    public void testPersistentUpdatingPrefs() throws Exception {
        final IPortletDefinitionId portletDefId = this.createDefaultPorltetDefinition();
        final String nodeId = "u1l1n1";
        
        //Mock setup
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(portalRequestUtils.getOriginalPortletOrPortalRequest(request)).thenReturn(request);
        
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(userInstance.getPerson()).thenReturn(person);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        
        when(userLayoutManager.getNode(nodeId)).thenReturn(node);
        when(node.getType()).thenReturn(LayoutNodeType.PORTLET);
        when(node.getChannelPublishId()).thenReturn(portletDefId.getStringId());


        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //Create the entity
                IPortletEntity portletEntity = portletEntityRegistry.getOrCreatePortletEntity(request, portletDefId, nodeId, 12);
                assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());

                return portletEntity.getPortletEntityId();
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                
                //Add a preference
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref", false, "value");
                preferences.add(portletPreference);
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(request, portletEntity);
                
                return null;
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                assertEquals(1, preferences.size());

                //add another preference
                final PortletPreferenceImpl portletPreference = new PortletPreferenceImpl("pref2", false, "valuea");
                preferences.add(portletPreference);
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(request, portletEntity);
                
                return null;
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                assertEquals(2, preferences.size());
                
                return null;
            }
        });
    }

    //interim with no prefs & not in db - noop
    @Test
    public void testInterimNoPrefs() throws Exception {
        final IPortletDefinitionId portletDefId = this.createDefaultPorltetDefinition();
        final String nodeId = "u1l1n1";
        
        //Mock setup
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(portalRequestUtils.getOriginalPortletOrPortalRequest(request)).thenReturn(request);
        
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(userInstance.getPerson()).thenReturn(person);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        
        when(userLayoutManager.getNode(nodeId)).thenReturn(node);
        when(node.getType()).thenReturn(LayoutNodeType.PORTLET);
        when(node.getChannelPublishId()).thenReturn(portletDefId.getStringId());


        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //Create the entity
                IPortletEntity portletEntity = portletEntityRegistry.getOrCreatePortletEntity(request, portletDefId, nodeId, 12);
                assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());

                return portletEntity.getPortletEntityId();
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                
                //Store the entity
                portletEntityRegistry.storePortletEntity(request, portletEntity);
                
                return null;
            }
        });


        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);

                //Verify it is still interim
                assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());
                
                return null;
            }
        });
    }

    //interim with prefs & not in db - create new & update, delete interim
    @Test
    public void testInterimAddingPrefs() throws Exception {
        final IPortletDefinitionId portletDefId = this.createDefaultPorltetDefinition();
        final String nodeId = "u1l1n1";
        
        //Mock setup
        final MockHttpServletRequest request = new MockHttpServletRequest();
        
        when(portalRequestUtils.getOriginalPortalRequest(request)).thenReturn(request);
        when(portalRequestUtils.getOriginalPortletOrPortalRequest(request)).thenReturn(request);
        
        when(userInstanceManager.getUserInstance(request)).thenReturn(userInstance);
        when(userInstance.getPreferencesManager()).thenReturn(preferencesManager);
        when(userInstance.getPerson()).thenReturn(person);
        when(preferencesManager.getUserLayoutManager()).thenReturn(userLayoutManager);
        
        when(userLayoutManager.getNode(nodeId)).thenReturn(node);
        when(node.getType()).thenReturn(LayoutNodeType.PORTLET);
        when(node.getChannelPublishId()).thenReturn(portletDefId.getStringId());


        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //Create the entity
                IPortletEntity portletEntity = portletEntityRegistry.getOrCreatePortletEntity(request, portletDefId, nodeId, 12);
                assertEquals(SessionPortletEntityImpl.class, portletEntity.getClass());

                return portletEntity.getPortletEntityId();
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                
                //Add a preference
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref", false, "value");
                preferences.add(portletPreference);
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(request, portletEntity);
                
                return null;
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(request, portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final List<IPortletPreference> preferences = portletEntity.getPortletPreferences();
                assertEquals(1, preferences.size());
                
                return null;
            }
        });
    }
}
