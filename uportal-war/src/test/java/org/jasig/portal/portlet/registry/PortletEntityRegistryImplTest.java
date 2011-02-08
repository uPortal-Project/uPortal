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

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import java.util.List;
import java.util.concurrent.Callable;

import org.jasig.portal.portlet.dao.IPortletDefinitionDao;
import org.jasig.portal.portlet.dao.IPortletEntityDao;
import org.jasig.portal.portlet.dao.IPortletTypeDao;
import org.jasig.portal.portlet.dao.jpa.BaseJpaDaoTest;
import org.jasig.portal.portlet.dao.jpa.PortletPreferenceImpl;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletDefinitionId;
import org.jasig.portal.portlet.om.IPortletEntity;
import org.jasig.portal.portlet.om.IPortletEntityId;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;
import org.jasig.portal.portlet.om.IPortletType;
import org.jasig.portal.url.IPortalRequestUtils;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortletEntityRegistryImplTest extends BaseJpaDaoTest {
    private IPortletTypeDao jpaPortletTypeDao;
    private IPortletDefinitionDao jpaPortletDefinitionDao;
    private IPortletEntityDao jpaPortletEntityDao;
    
    
    public PortletEntityRegistryImplTest() {
        this.setDependencyCheck(false);
    }
    
    @Override
    protected String[] getConfigLocations() {
        return new String[] {"classpath:jpaTestApplicationContext.xml"};
    }

    public void setJpaPortletEntityDao(IPortletEntityDao jpaPortletEntityDao) {
        this.jpaPortletEntityDao = jpaPortletEntityDao;
    }
    public void setJpaPortletDefinitionDao(IPortletDefinitionDao dao) {
        this.jpaPortletDefinitionDao = dao;
    }
    public void setJpaChannelTypeDao(IPortletTypeDao jpaChannelTypeDao) {
        this.jpaPortletTypeDao = jpaChannelTypeDao;
    }

    @SuppressWarnings("deprecation")
    @Override
    protected void onSetUp() throws Exception {
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

    @SuppressWarnings("deprecation")
    protected IPortletDefinitionId createDefaultPorltetDefinition() {
        return this.execute(new Callable<IPortletDefinitionId>() {
            @Override
            public IPortletDefinitionId call() throws Exception {
                final IPortletType channelType = jpaPortletTypeDao.createPortletType("BaseType", "foobar");
                
                //Create a definition
                final IPortletDefinition portletDef = jpaPortletDefinitionDao.createPortletDefinition(channelType, "fname1", "Test Portlet 1", "Test Portlet 1 Title", "/context1", "portletName1", false);
                

                return portletDef.getPortletDefinitionId();
            }
        });
    }
    
    //persistent with prefs & not in db - create new & update
    public void testPersistentWithPrefsNotInDb() throws Throwable {
        final IPortletDefinitionId portDefId1 = this.createDefaultPorltetDefinition();
        
        //Mock setup
        final IPortalRequestUtils requestUtils = createMock(IPortalRequestUtils.class);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(); //init the session
        
        expect(requestUtils.getCurrentPortalRequest()).andReturn(request).anyTimes();
        
        replay(requestUtils);
        
        final PortletEntityRegistryImpl portletEntityRegistry = new PortletEntityRegistryImpl();
        portletEntityRegistry.setPortletEntityDao(this.jpaPortletEntityDao);
        portletEntityRegistry.setPortalRequestUtils(requestUtils);

        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //T1 - Create the entity
                final IPortletEntity portletEntity = portletEntityRegistry.createPortletEntity(portDefId1, "u1l1n1", 12);;
                assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());
                
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
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                
                //Add a preference
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref", false, "value");
                preferences.add(portletPreference);
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(portletEntity);
                
                return null;
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //T1 - Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                assertEquals(1, preferences.size());
                
                //T2 - get the entity and add preferences
                final IPortletEntityId portletEntityId = executeInThread("T2.1", new Callable<IPortletEntityId>() {
                    @Override
                    public IPortletEntityId call() throws Exception {
                        //T2 - Get entity
                        final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity("u1l1n1", 12);
                        assertEquals(portletEntity, portletEntity);
        
                        //T2 - add preference
                        final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                        final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                        preferences.clear();
                        
                        //T2 - Store the entity
                        portletEntityRegistry.storePortletEntity(portletEntity);
                        
                        return portletEntity.getPortletEntityId();
                    }
                });

                //T2 - verify entity was made persistent
                executeInThread("T2.2", new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        
                        //T2 - Verify it was converted from persistent to interim
                        final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                        assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());
                        final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                        final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                        assertEquals(0, preferences.size());
                        
                        return null;
                    }
                });
        

                //T1 - add preference 2
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref2", false, "value");
                preferences.add(portletPreference);
        
                //T1 - Store the entity
                portletEntityRegistry.storePortletEntity(portletEntity);
                
                return null;
            }
        });
        
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //T1 - Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                assertEquals(2, preferences.size());
                
                return null;
            }
        });
        
        verify(requestUtils);
    }
    
    //persistent with no prefs & not in db - noop
    public void testPersistentNoPrefsNotInDb() throws Throwable {
        final IPortletDefinitionId portDefId1 = this.createDefaultPorltetDefinition();
        
        //Mock setup
        final IPortalRequestUtils requestUtils = createMock(IPortalRequestUtils.class);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(); //init the session
        
        expect(requestUtils.getCurrentPortalRequest()).andReturn(request).anyTimes();
        
        replay(requestUtils);
        
        final PortletEntityRegistryImpl portletEntityRegistry = new PortletEntityRegistryImpl();
        portletEntityRegistry.setPortletEntityDao(this.jpaPortletEntityDao);
        portletEntityRegistry.setPortalRequestUtils(requestUtils);

        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //T1 - Create the entity
                final IPortletEntity portletEntity = portletEntityRegistry.createPortletEntity(portDefId1, "u1l1n1", 12);;
                assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());
                
                return portletEntity.getPortletEntityId();
            }
        });


        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                
                //Add a preference
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref", false, "value");
                preferences.add(portletPreference);
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(portletEntity);
                
                return null;
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //T1 - Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                assertEquals(1, preferences.size());
                
                //T2 - get the entity and add preferences
                final IPortletEntityId portletEntityId = executeInThread("T2.1", new Callable<IPortletEntityId>() {
                    @Override
                    public IPortletEntityId call() throws Exception {
                        //T2 - Get entity
                        final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity("u1l1n1", 12);
                        assertEquals(portletEntity, portletEntity);
        
                        //T2 - remove preferences
                        final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                        final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                        preferences.clear();
                        
                        //T2 - Store the entity
                        portletEntityRegistry.storePortletEntity(portletEntity);
                        
                        return portletEntity.getPortletEntityId();
                    }
                });

                //T2 - verify entity was made persistent
                executeInThread("T2.2", new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        
                        //T2 - Verify it was converted from persistent to interim
                        IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                        assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());
                        IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                        List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                        assertEquals(0, preferences.size());
                        
                        return null;
                    }
                });
        

                //T1 - remove all preferences
                preferences.clear();
        
                //T1 - Store the entity
                portletEntityRegistry.storePortletEntity(portletEntity);
                
                return null;
            }
        });
        
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //T1 - Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                assertEquals(0, preferences.size());
                
                return null;
            }
        });
        
        verify(requestUtils);
    }
    
    //interim with no prefs & in db - delete db version
    public void testInterimNoPrefsAlreadyPersistent() throws Throwable {
        final IPortletDefinitionId portDefId1 = this.createDefaultPorltetDefinition();
        
        //Mock setup
        final IPortalRequestUtils requestUtils = createMock(IPortalRequestUtils.class);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(); //init the session
        
        expect(requestUtils.getCurrentPortalRequest()).andReturn(request).anyTimes();
        
        replay(requestUtils);
        
        final PortletEntityRegistryImpl portletEntityRegistry = new PortletEntityRegistryImpl();
        portletEntityRegistry.setPortletEntityDao(this.jpaPortletEntityDao);
        portletEntityRegistry.setPortalRequestUtils(requestUtils);

        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //T1 - Create the entity
                final IPortletEntity portletEntity = portletEntityRegistry.createPortletEntity(portDefId1, "u1l1n1", 12);;
                assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());
                
                //T2 - get the entity and add preferences
                final IPortletEntityId portletEntityId = executeInThread("T2.1", new Callable<IPortletEntityId>() {
                    @Override
                    public IPortletEntityId call() throws Exception {
                        //T2 - Get entity
                        final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity("u1l1n1", 12);
                        assertEquals(portletEntity, portletEntity);
        
                        //T2 - Add a preference
                        final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                        final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                        final IPortletPreference portletPreference = new PortletPreferenceImpl("pref2", false, "value");
                        preferences.add(portletPreference);
                        
                        //T2 - Store the entity
                        portletEntityRegistry.storePortletEntity(portletEntity);
                        
                        return portletEntity.getPortletEntityId();
                    }
                });

                //T2 - verify entity was made persistent
                executeInThread("T2.2", new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        
                        //T2 - Verify it was converted from interim to persistent
                        IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                        assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                        IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                        List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                        assertEquals(1, preferences.size());
                        
                        return null;
                    }
                });
        
                //T1 - clear preferences
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                preferences.clear();
        
                //T1 - Store the entity
                portletEntityRegistry.storePortletEntity(portletEntity);
                
                return portletEntity.getPortletEntityId();
            }
        });
        
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //T1 - Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                assertEquals(0, preferences.size());
                
                return null;
            }
        });
        
        verify(requestUtils);
    }
    
    //interim with prefs & in db - get db version & update
    public void testInterimAddingPrefsAlreadyPersistent() throws Throwable {
        final IPortletDefinitionId portDefId1 = this.createDefaultPorltetDefinition();
        
        //Mock setup
        final IPortalRequestUtils requestUtils = createMock(IPortalRequestUtils.class);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(); //init the session
        
        expect(requestUtils.getCurrentPortalRequest()).andReturn(request).anyTimes();
        
        replay(requestUtils);
        
        final PortletEntityRegistryImpl portletEntityRegistry = new PortletEntityRegistryImpl();
        portletEntityRegistry.setPortletEntityDao(this.jpaPortletEntityDao);
        portletEntityRegistry.setPortalRequestUtils(requestUtils);

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
                final IPortletEntity portletEntity = portletEntityRegistry.createPortletEntity(portDefId1, "u1l1n1", 12);;
                assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());
                
                //T2 - get the entity and add preferences
                final IPortletEntityId portletEntityId = executeInThread("T2.1", new Callable<IPortletEntityId>() {
                    @Override
                    public IPortletEntityId call() throws Exception {
                        //T2 - Get entity
                        final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity("u1l1n1", 12);
                        assertEquals(portletEntity, portletEntity);
        
                        //T2 - Add a preference
                        final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                        final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                        final IPortletPreference portletPreference = new PortletPreferenceImpl("pref2", false, "value");
                        preferences.add(portletPreference);
                        
                        //T2 - Store the entity
                        portletEntityRegistry.storePortletEntity(portletEntity);
                        
                        return portletEntity.getPortletEntityId();
                    }
                });

                //T2 - verify entity was made persistent
                executeInThread("T2.2", new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        
                        //T2 - Verify it was converted from interim to persistent
                        IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                        assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                        IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                        List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                        assertEquals(1, preferences.size());
                        
                        return null;
                    }
                });
        
                //T1 - Add a preference
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref1", false, "value");
                preferences.add(portletPreference);
        
                //T1 - Store the entity
                portletEntityRegistry.storePortletEntity(portletEntity);
                
                return portletEntity.getPortletEntityId();
            }
        });
        
        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //T1 - Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                assertEquals(2, preferences.size());
                
                return null;
            }
        });
        
        verify(requestUtils);
    }
    
    //persistent with no prefs & in db - delete & create interim
    public void testPersistentRemovePrefs() throws Exception {
        //Mock setup
        final IPortalRequestUtils requestUtils = createMock(IPortalRequestUtils.class);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(); //init the session

        expect(requestUtils.getCurrentPortalRequest()).andReturn(request).anyTimes();

        replay(requestUtils);

        final PortletEntityRegistryImpl portletEntityRegistry = new PortletEntityRegistryImpl();
        portletEntityRegistry.setPortletEntityDao(this.jpaPortletEntityDao);
        portletEntityRegistry.setPortalRequestUtils(requestUtils);

        final IPortletDefinitionId portletDefId = this.createDefaultPorltetDefinition();


        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //Create the entity
                IPortletEntity portletEntity = portletEntityRegistry.createPortletEntity(portletDefId, "u1l1n1", 12);
                assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());

                return portletEntity.getPortletEntityId();
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                
                //Add a preference
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref", false, "value");
                preferences.add(portletPreference);
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(portletEntity);
                
                return null;
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                assertEquals(1, preferences.size());

                //remove all preferences
                preferences.clear();
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(portletEntity);
                
                return null;
            }
        });


        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //Verify it switched from persistent to interim
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                assertEquals(0, preferences.size());
                
                return null;
            }
        });

        verify(requestUtils);
    }

    //persistent with prefs & in db - update
    public void testPersistentUpdatingPrefs() throws Exception {
        //Mock setup
        final IPortalRequestUtils requestUtils = createMock(IPortalRequestUtils.class);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(); //init the session

        expect(requestUtils.getCurrentPortalRequest()).andReturn(request).anyTimes();

        replay(requestUtils);

        final PortletEntityRegistryImpl portletEntityRegistry = new PortletEntityRegistryImpl();
        portletEntityRegistry.setPortletEntityDao(this.jpaPortletEntityDao);
        portletEntityRegistry.setPortalRequestUtils(requestUtils);

        final IPortletDefinitionId portletDefId = this.createDefaultPorltetDefinition();


        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //Create the entity
                IPortletEntity portletEntity = portletEntityRegistry.createPortletEntity(portletDefId, "u1l1n1", 12);
                assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());

                return portletEntity.getPortletEntityId();
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                
                //Add a preference
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref", false, "value");
                preferences.add(portletPreference);
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(portletEntity);
                
                return null;
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                assertEquals(1, preferences.size());

                //add another preference
                final PortletPreferenceImpl portletPreference = new PortletPreferenceImpl("pref2", false, "valuea");
                preferences.add(portletPreference);
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(portletEntity);
                
                return null;
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                assertEquals(2, preferences.size());
                
                return null;
            }
        });

        verify(requestUtils);
    }

    //interim with no prefs & not in db - noop
    public void testInterimNoPrefs() throws Exception {
        //Mock setup
        final IPortalRequestUtils requestUtils = createMock(IPortalRequestUtils.class);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(); //init the session

        expect(requestUtils.getCurrentPortalRequest()).andReturn(request).anyTimes();

        replay(requestUtils);

        final PortletEntityRegistryImpl portletEntityRegistry = new PortletEntityRegistryImpl();
        portletEntityRegistry.setPortletEntityDao(this.jpaPortletEntityDao);
        portletEntityRegistry.setPortalRequestUtils(requestUtils);

        final IPortletDefinitionId portletDefId = this.createDefaultPorltetDefinition();


        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //Create the entity
                IPortletEntity portletEntity = portletEntityRegistry.createPortletEntity(portletDefId, "u1l1n1", 12);
                assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());

                return portletEntity.getPortletEntityId();
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                
                //Store the entity
                portletEntityRegistry.storePortletEntity(portletEntity);
                
                return null;
            }
        });


        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);

                //Verify it is still interim
                assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());
                
                return null;
            }
        });

        verify(requestUtils);
    }

    //interim with prefs & not in db - create new & update, delete interim
    public void testInterimAddingPrefs() throws Exception {
        //Mock setup
        final IPortalRequestUtils requestUtils = createMock(IPortalRequestUtils.class);
        final MockHttpServletRequest request = new MockHttpServletRequest();
        request.getSession(); //init the session

        expect(requestUtils.getCurrentPortalRequest()).andReturn(request).anyTimes();

        replay(requestUtils);

        final PortletEntityRegistryImpl portletEntityRegistry = new PortletEntityRegistryImpl();
        portletEntityRegistry.setPortletEntityDao(this.jpaPortletEntityDao);
        portletEntityRegistry.setPortalRequestUtils(requestUtils);

        final IPortletDefinitionId portletDefId = this.createDefaultPorltetDefinition();


        final IPortletEntityId portletEntityId = this.execute(new Callable<IPortletEntityId>() {
            @Override
            public IPortletEntityId call() throws Exception {
                //Create the entity
                IPortletEntity portletEntity = portletEntityRegistry.createPortletEntity(portletDefId, "u1l1n1", 12);
                assertEquals(InterimPortletEntityImpl.class, portletEntity.getClass());

                return portletEntity.getPortletEntityId();
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                
                //Add a preference
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                final IPortletPreference portletPreference = new PortletPreferenceImpl("pref", false, "value");
                preferences.add(portletPreference);
        
                //Store the entity
                portletEntityRegistry.storePortletEntity(portletEntity);
                
                return null;
            }
        });

        this.execute(new Callable<Object>() {
            @Override
            public Object call() throws Exception {
                //Verify it was converted from interim to persistent
                final IPortletEntity portletEntity = portletEntityRegistry.getPortletEntity(portletEntityId);
                assertEquals(PersistentPortletEntityWrapper.class, portletEntity.getClass());
                final IPortletPreferences portletPreferences = portletEntity.getPortletPreferences();
                final List<IPortletPreference> preferences = portletPreferences.getPortletPreferences();
                assertEquals(1, preferences.size());
                
                return null;
            }
        });

        verify(requestUtils);
    }
}
