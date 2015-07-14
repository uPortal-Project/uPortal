/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.jasig.portal.io.xml.portlet

import org.jasig.portal.groups.GroupServiceConfiguration
import org.jasig.portal.groups.GroupsException
import org.jasig.portal.groups.ICompositeGroupService
import org.jasig.portal.groups.ICompositeGroupServiceFactory
import org.jasig.portal.groups.IEntity
import org.jasig.portal.groups.IEntityGroup
import org.jasig.portal.io.xml.portlettype.ExternalPermissionDefinition
import org.jasig.portal.mock.portlet.om.MockPortletDefinitionId;
import org.jasig.portal.portlet.dao.IPortletDefinitionDao
import org.jasig.portal.portlet.dao.jpa.PortletDefinitionImpl
import org.jasig.portal.portlet.dao.jpa.PortletTypeImpl
import org.jasig.portal.portlet.om.IPortletDefinition
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry
import org.jasig.portal.portlet.registry.IPortletTypeRegistry
import org.jasig.portal.security.IAuthorizationPrincipal
import org.jasig.portal.security.IAuthorizationService
import org.jasig.portal.security.IPermission
import org.jasig.portal.security.IUpdatingPermissionManager
import org.jasig.portal.spring.locator.AbstractBeanLocator
import org.jasig.portal.spring.locator.AuthorizationServiceLocator
import org.jasig.portal.xml.PortletDescriptor;
import spock.lang.Specification
import spock.lang.Subject

import java.lang.reflect.InvocationHandler
import java.lang.reflect.Method
import java.lang.reflect.Proxy


/**
 * @author Josh Helmer, jhelmer.unicon.net
 */
public class PortletDefinitionImporterExporterTest extends Specification {
    private static ICompositeGroupService staticGroupService;
    private static AuthorizationServiceInvocationHandler authServiceInvocationHandler = new AuthorizationServiceInvocationHandler();
    private static IAuthorizationService proxyAuthorizationService;

    private IPortletTypeRegistry typeRegistry = Mock();
    private IPortletDefinitionDao definitionDao = Mock();
    private IPortletCategoryRegistry categoryRegistry = Mock();
    private ICompositeGroupService compositeGroupService = Mock();
    private IEntity portletDefEntity = Mock();
    private IAuthorizationService authorizationService = Mock();
    private IUpdatingPermissionManager updatingPermissionManager = Mock();

    @Subject
    private PortletDefinitionImporterExporter importer = new PortletDefinitionImporterExporter(
        portletTypeRegistry: typeRegistry,
        portletDefinitionDao: definitionDao,
        portletCategoryRegistry: categoryRegistry
    );


    def setupSpec() {
        // grr.  hack around some of the static GroupService methods...
        // This is FAR from ideal.  If any other tests need to mock the group
        // service, this will get in the way (or get broken).  For now, it does
        // not cause conflicts on other tests.  Long term, need to convert
        // GroupService to a spring bean, but I think that may be quite involved.
        def groupServiceConfig = GroupServiceConfiguration.getConfiguration();
        def attrs = groupServiceConfig.getAttributes();
        attrs.put('compositeFactory', MockCompositeGroupServiceFactory.class.getName());

        // hack around the static AuthorizationService methods...
        proxyAuthorizationService = Proxy.newProxyInstance(
            getClass().getClassLoader(),
            (Class[])[ IAuthorizationService.class ].toArray(),
            authServiceInvocationHandler
        );

        def locator = new AbstractBeanLocator<IAuthorizationService>(proxyAuthorizationService, IAuthorizationService.class) {
            @Override
            protected void setLocator(AbstractBeanLocator<IAuthorizationService> locator) {}
            @Override
            protected AbstractBeanLocator<IAuthorizationService> getLocator() { return null };
        };
        new AuthorizationServiceLocator(proxyAuthorizationService).setLocator(locator);
    }


    def setup() {
        // point the static var at the composite service object so that the
        // guts of the GroupService can be mocked (since GroupService can't be
        // directly mocked.
        staticGroupService = compositeGroupService;

        // hack to mock the guts of the AuthorizationService
        authServiceInvocationHandler.service = authorizationService;
    }


    def 'should support importing simple portlets'() {
        given:
            def portletType = new PortletTypeImpl('Portlet', 'CpdUri');
            def portletDef = null;

        and: 'I setup a sample portlet definition'
            def input = new ExternalPortletDefinition(
                name: 'test',
                title: 'title',
                fname: 'test-new',
                desc: 'desc',
                type: 'Portlet',
                portletDescriptor: new PortletDescriptor(webAppName: "test", portletName: "test", isFramework: false),
                categories: []
            );

        when: 'I import the portlet'
            importer.importData(input);

        then: 'I see that the appropriate interactions were called'
            1 * typeRegistry.getPortletType('Portlet') >> portletType;

            2 * definitionDao.getPortletDefinitionByFname(_) >> { return portletDef; }
            1 * definitionDao.createPortletDefinition(_, _, _, _, _, _, _) >> { type, fname, name, title, app, portlet, framework ->
                portletDef = new PortletDefinitionImpl(portletType, 'fname', 'name', 'title', 'webapp', 'portletName', true);
                portletDef.portletDefinitionId = new MockPortletDefinitionId(100l);
                return portletDef;
            };
            1 * definitionDao.updatePortletDefinition(_) >> { IPortletDefinition pd ->
                return pd;
            };
            1 * compositeGroupService.getEntity(_, _, _) >> portletDefEntity;

            1 * portletDefEntity.getAllContainingGroups() >> [].iterator();

            1 * authorizationService.newUpdatingPermissionManager(_) >> updatingPermissionManager;
            1 * updatingPermissionManager.getPermissions(ExternalPermissionDefinition.SUBSCRIBE.getActivity(), _) >> [];
            1 * updatingPermissionManager.removePermissions(_);
            1 * updatingPermissionManager.addPermissions({ IPermission[] permArray ->
                return permArray.size() == 0;
            });
    }


    def 'should support updating existing portlets'() {
        given: 'A portlet definition exists'
            def portletType = new PortletTypeImpl('Portlet', 'CpdUri');
            def portletDef = new PortletDefinitionImpl(portletType, 'fname', 'name', 'title', 'webapp', 'portletName', true);
            portletDef.portletDefinitionId = new MockPortletDefinitionId(100l);

            // groups assigned to old entity
            IEntityGroup oldGroupStudent = Mock();
            IEntityGroup oldGroupStaff = Mock();

            // groups assigned to new entity
            IEntityGroup newGroupPortalAdmin = Mock();
            IEntityGroup newGroupTeacher = Mock();

            // authorization pricinpals for the new permissions
            IAuthorizationPrincipal newPrincipalPortalAdmin = Mock();
            IAuthorizationPrincipal newPrincipalTeacher = Mock();

            // actual permission object that will be created
            IPermission newPermissionPortalAdmin = Mock();
            IPermission newPermissionTeacher = Mock();

        and: 'I setup an external portlet definition with the same fname'
            def input = new ExternalPortletDefinition(
                name: 'test',
                title: 'title',
                fname: 'fname',
                desc: 'desc',
                type: 'Portlet',
                groups: [ "Portal Administrator", "Teacher" ],
                portletDescriptor: new PortletDescriptor(webAppName: "test", portletName: "test", isFramework: false),
                categories: []
            );

        when: 'I import the portlet'
            importer.importData(input);

        then: 'I see the appropriate interactions were called'
            1 * typeRegistry.getPortletType('Portlet') >> portletType;

            2 * definitionDao.getPortletDefinitionByFname(_) >> { return portletDef; }
            0 * definitionDao.createPortletDefinition(_, _, _, _, _, _, _);

            1 * definitionDao.updatePortletDefinition(_) >> { IPortletDefinition pd ->
                return pd;
            };
            1 * compositeGroupService.getEntity(_, _, _) >> portletDefEntity;
            1 * compositeGroupService.findGroup("Portal Administrator") >> newGroupPortalAdmin;
            1 * compositeGroupService.findGroup("Teacher") >> newGroupTeacher;

            1 * oldGroupStudent.removeMember(portletDefEntity);
            1 * oldGroupStaff.removeMember(portletDefEntity);
            1 * portletDefEntity.getAllContainingGroups() >> {
                return [
                    oldGroupStudent,
                    oldGroupStaff
                ].iterator();
            };

            1 * authorizationService.newUpdatingPermissionManager(_) >> updatingPermissionManager;
            1 * authorizationService.newPrincipal(newGroupPortalAdmin) >> newPrincipalPortalAdmin;
            1 * authorizationService.newPrincipal(newGroupTeacher) >> newPrincipalTeacher;
            1 * updatingPermissionManager.getPermissions(ExternalPermissionDefinition.SUBSCRIBE.getActivity(), _) >> [];
            1 * updatingPermissionManager.newPermission(newPrincipalPortalAdmin) >> newPermissionPortalAdmin
            1 * updatingPermissionManager.newPermission(newPrincipalTeacher) >> newPermissionTeacher

            1 * newPermissionPortalAdmin.setActivity(ExternalPermissionDefinition.SUBSCRIBE.activity);
            1 * newPermissionTeacher.setActivity(ExternalPermissionDefinition.SUBSCRIBE.activity);

            1 * updatingPermissionManager.removePermissions(_);
            1 * updatingPermissionManager.addPermissions({ IPermission[] permArray ->
                return permArray.size() == 2;
            });
    }


    def 'should support extended permissions'() {
        given: 'A portlet definition exists'
            def portletType = new PortletTypeImpl('Portlet', 'CpdUri');
            def portletDef = new PortletDefinitionImpl(portletType, 'fname', 'name', 'title', 'webapp', 'portletName', true);
            portletDef.portletDefinitionId = new MockPortletDefinitionId(100l);

            // groups assigned to old entity
            IEntityGroup oldGroupStudent = Mock();
            IEntityGroup oldGroupStaff = Mock();

            // groups assigned to new entity
            IEntityGroup newGroupPortalAdmin = Mock();
            IEntityGroup newGroupTeacher = Mock();
            IEntityGroup newGroupStudent = Mock();

            // authorization pricinpals for the new permissions
            IAuthorizationPrincipal newPrincipalPortalAdmin = Mock();
            IAuthorizationPrincipal newPrincipalTeacher = Mock();
            IAuthorizationPrincipal newPrincipalStudent = Mock();

            // actual permission object that will be created
            IPermission newPermissionPortalAdmin = Mock();
            IPermission newPermissionTeacher = Mock();
            IPermission newPermissionStudent = Mock();

        and: 'I setup an external portlet definition with the same fname'
            def input = new ExternalPortletDefinition(
                name: 'test',
                title: 'title',
                fname: 'fname',
                desc: 'desc',
                type: 'Portlet',
                groups: [ "Portal Administrator", "Teacher" ],
                permissions: new ExternalPermissions(
                    permissions: [
                        new ExternalPermissionMemberList(
                            system: ExternalPermissionDefinition.BROWSE.system,
                            activity: ExternalPermissionDefinition.BROWSE.activity,
                            groups: [
                                "Portal Administrator",
                                "Teacher",
                                "Student"
                            ]
                        )
                    ]
                ),
                portletDescriptor: new PortletDescriptor(webAppName: "test", portletName: "test", isFramework: false),
                categories: []
            );

        when: 'I import the portlet'
            importer.importData(input);

        then: 'I see the appropriate interactions were called'
            1 * typeRegistry.getPortletType('Portlet') >> portletType;

            2 * definitionDao.getPortletDefinitionByFname(_) >> { return portletDef; }
            0 * definitionDao.createPortletDefinition(_, _, _, _, _, _, _);

            1 * definitionDao.updatePortletDefinition(_) >> { IPortletDefinition pd ->
                return pd;
            };
            1 * compositeGroupService.getEntity(_, _, _) >> portletDefEntity;
            2 * compositeGroupService.findGroup("Portal Administrator") >> newGroupPortalAdmin;
            2 * compositeGroupService.findGroup("Teacher") >> newGroupTeacher;
            1 * compositeGroupService.findGroup("Student") >> newGroupStudent;

            1 * oldGroupStudent.removeMember(portletDefEntity);
            1 * oldGroupStaff.removeMember(portletDefEntity);
            1 * portletDefEntity.getAllContainingGroups() >> {
                return [
                    oldGroupStudent,
                    oldGroupStaff
                ].iterator();
            };

            1 * authorizationService.newUpdatingPermissionManager(_) >> updatingPermissionManager;
            2 * authorizationService.newPrincipal(newGroupPortalAdmin) >> newPrincipalPortalAdmin;
            2 * authorizationService.newPrincipal(newGroupTeacher) >> newPrincipalTeacher;
            1 * authorizationService.newPrincipal(newGroupStudent) >> newPrincipalStudent;

            1 * updatingPermissionManager.getPermissions(ExternalPermissionDefinition.SUBSCRIBE.activity, _) >> [];
            1 * updatingPermissionManager.getPermissions(ExternalPermissionDefinition.BROWSE.activity, _) >> [];

            2 * updatingPermissionManager.newPermission(newPrincipalPortalAdmin) >> newPermissionPortalAdmin
            2 * updatingPermissionManager.newPermission(newPrincipalTeacher) >> newPermissionTeacher
            1 * updatingPermissionManager.newPermission(newPrincipalStudent) >> newPermissionStudent

            1 * newPermissionPortalAdmin.setActivity(ExternalPermissionDefinition.SUBSCRIBE.activity);
            1 * newPermissionTeacher.setActivity(ExternalPermissionDefinition.SUBSCRIBE.activity);

            1 * newPermissionPortalAdmin.setActivity(ExternalPermissionDefinition.BROWSE.activity);
            1 * newPermissionTeacher.setActivity(ExternalPermissionDefinition.BROWSE.activity);
            1 * newPermissionStudent.setActivity(ExternalPermissionDefinition.BROWSE.activity);

            2 * updatingPermissionManager.removePermissions(_);
            1 * updatingPermissionManager.addPermissions({ IPermission[] permArray ->
                // expect this to be called with 2 subscribe and 3 browse permissions...
                return permArray.size() == 5;
            });
    }


    /* Util classes necessary to work around some services that still have static methods */

    /**
     * Authorization service invocation handler for proxying a IAuthorizationService.  This just
     * allows me to switch out the mocked authorization service implementation for each test.  This
     * is required due to the use of static AuthorizationService methods.
     */
    private static class AuthorizationServiceInvocationHandler implements InvocationHandler {
        private IAuthorizationService service;

        public void setService(final IAuthorizationService service) {
            this.service = service;
        }


        @Override
        Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            if (service != null) {
                return method.invoke(service, args);
            }

            return null;
        }
    }

    /**
     * Mocked CompositeGroupServiceFactory impl.  Creates a proxy CompositeGroupService instance
     * that can survive between specs, but have the underlying impl switched out.   Needs to be a
     * real class (as opposed to using mocking or Map coersion) because the GroupService instantiates
     * this via reflection.
     */
    public static class MockCompositeGroupServiceFactory implements ICompositeGroupServiceFactory {
        @Override
        public ICompositeGroupService newGroupService() throws GroupsException {
            // Since GroupService creates only 1 ICompositeGroupService and binds it to a singleton,
            // I have to return a concrete instance that proxies to a mock that can
            // be recreated per tests (since Mocks can't be static of @Shared)
            return Proxy.newProxyInstance(getClass().getClassLoader(),
                (Class[]) [ ICompositeGroupService.class ].toArray(),
                new InvocationHandler() {

                    @Override
                    Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (staticGroupService != null) {
                            return method.invoke(staticGroupService, args);
                        }

                        return null;
                    }
                }
            );
        }
    }
}
