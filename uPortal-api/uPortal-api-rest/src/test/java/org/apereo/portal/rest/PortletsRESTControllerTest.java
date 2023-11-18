package org.apereo.portal.rest;

import static org.apereo.portal.security.IAuthorizationService.PortletPermissionType.MANAGE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.EntityIdentifier;
import org.apereo.portal.UserPreferencesManager;
import org.apereo.portal.layout.IUserLayout;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.portlet.dao.jpa.PortletTypeImpl;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletType;
import org.apereo.portal.portlet.om.PortletLifecycleState;
import org.apereo.portal.portlet.registry.IPortletCategoryRegistry;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlets.favorites.FavoritesUtils;
import org.apereo.portal.security.*;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.springframework.web.servlet.ModelAndView;

@RunWith(MockitoJUnitRunner.class)
public class PortletsRESTControllerTest {

    @InjectMocks private PortletsRESTController portletsRESTController;

    @Mock private IAuthorizationService authorizationService;
    @Mock private IAuthorizationPrincipal authorizationPrincipal;
    @Mock private IPortletCategoryRegistry portletCategoryRegistry;
    @Mock private IPortletDefinitionRegistry portletDefinitionRegistry;
    @Mock private IPerson user;
    @Mock private IPersonManager personManager;
    @Mock private EntityIdentifier userEntityIdentifier;
    @Mock private FavoritesUtils favoritesUtils;
    @Mock private UserPreferencesManager userPreferencesManager;
    @Mock private IUserInstanceManager userInstanceManager;
    @Mock private IUserLayoutManager userLayoutManager;

    @Mock private HttpServletRequest request;
    @Mock private IPortletDefinition portletDefinition1, portletDefinition2, portletDefinition3;
    @Mock private IPortletType portletType;
    @Mock private IUserInstance userInstance;
    @Mock private IUserLayout userLayout;

    private static final String entityIdentifierKey = "entityIdentifierKey";
    private static final long portletDefinitionId1Long = 1L;
    private static final long portletDefinitionId2Long = 2L;
    private static final long portletDefinitionId3Long = 3L;
    private static final IPortletDefinitionId portletDefinitionId1 =
            new PortletDefinitionId(portletDefinitionId1Long);
    private static final IPortletDefinitionId portletDefinitionId2 =
            new PortletDefinitionId(portletDefinitionId2Long);
    private static final IPortletDefinitionId portletDefinitionId3 =
            new PortletDefinitionId(portletDefinitionId3Long);
    private List<IPortletDefinition> portletsFromRegistry;

    private PortletsRESTController spyPortletRestController;

    @Before
    public void setUp() throws Exception {
        spyPortletRestController = spy(this.portletsRESTController);

        this.portletsFromRegistry = new ArrayList<>();
        this.userEntityIdentifier = new EntityIdentifier(entityIdentifierKey, IPerson.class);
        this.portletType = new PortletTypeImpl("portletType", "portletType");
        this.setupMockPortletDefinition(portletDefinition1, portletDefinitionId1);
        given(this.portletDefinitionRegistry.getAllPortletDefinitions())
                .willReturn(portletsFromRegistry);
        given(this.personManager.getPerson(this.request)).willReturn(this.user);
        given(this.user.getEntityIdentifier()).willReturn(this.userEntityIdentifier);
        given(
                        this.authorizationService.newPrincipal(
                                this.userEntityIdentifier.getKey(),
                                this.userEntityIdentifier.getType()))
                .willReturn(this.authorizationPrincipal);
        this.setupMockPortletDefinition(this.portletDefinition1, portletDefinitionId1);
        this.setupMockPortletDefinition(this.portletDefinition2, portletDefinitionId2);
        this.setupMockPortletDefinition(this.portletDefinition3, portletDefinitionId3);
        this.givenPortletDefinitionsInRegistry(
                this.portletDefinition1, this.portletDefinition2, this.portletDefinition3);
    }

    /*       @Test
    public void testGetPortletsWithNoPermissionsTypeSpecifiedDefaultsToManagePermissionsType() {
        this.givenUserHasPermissionForPortlets(
                this.user, MANAGE, this.portletDefinition1, this.portletDefinition3);
        final ModelAndView mav = this.portletsRESTController.getPortlets(request);
        this.verifyPortletResults(mav, this.portletDefinition1, this.portletDefinition3);
    }

    @Test
    public void testGetPortletsWithManagePermissionsTypeSpecified() {
        this.givenRequestSpecifiesPermissionType(MANAGE);
        this.givenUserHasPermissionForPortlets(
                this.user, MANAGE, this.portletDefinition1, this.portletDefinition3);
        final ModelAndView mav = this.portletsRESTController.getPortlets(request);
        this.verifyPortletResults(mav, this.portletDefinition1, this.portletDefinition3);
    }

    @Test
    public void testGetPortletsWithBrowseManagePermissionsTypeSpecified() {
        this.givenRequestSpecifiesPermissionType(BROWSE);
        this.givenUserHasPermissionForPortlets(
                this.user, BROWSE, this.portletDefinition1, this.portletDefinition3);
        final ModelAndView mav = this.portletsRESTController.getPortlets(request);
        this.verifyPortletResults(mav, this.portletDefinition1, this.portletDefinition3);
    }

    @Test
    public void testGetPortletsWithConfigurePermissionsTypeSpecified() {
        this.givenRequestSpecifiesPermissionType(CONFIGURE);
        this.givenUserHasPermissionForPortlets(
                this.user, CONFIGURE, this.portletDefinition1, this.portletDefinition3);
        final ModelAndView mav = this.portletsRESTController.getPortlets(request);
        this.verifyPortletResults(mav, this.portletDefinition1, this.portletDefinition3);
    }

    @Test
    public void testGetPortletsWithRenderPermissionsTypeSpecified() {
        this.givenRequestSpecifiesPermissionType(RENDER);
        this.givenUserHasPermissionForPortlets(
                this.user, RENDER, this.portletDefinition1, this.portletDefinition3);
        final ModelAndView mav = this.portletsRESTController.getPortlets(request);
        this.verifyPortletResults(mav, this.portletDefinition1, this.portletDefinition3);
    }

    @Test
    public void testGetPortletsWithSubscribePermissionsTypeSpecified() {
        this.givenRequestSpecifiesPermissionType(SUBSCRIBE);
        this.givenUserHasPermissionForPortlets(
                this.user, SUBSCRIBE, this.portletDefinition1, this.portletDefinition3);
        final ModelAndView mav = this.portletsRESTController.getPortlets(request);
        this.verifyPortletResults(mav, this.portletDefinition1, this.portletDefinition3);
    }*/

    @Test
    public void testGetPortletsWhenLimitingToFavoritePortlets() {
        this.givenRequestSpecifiesFavoriteFlag(true);
        this.givenUserHasPermissionForPortlets(
                this.user,
                MANAGE,
                this.portletDefinition1,
                this.portletDefinition2,
                this.portletDefinition3);
        this.givenUserHasFavoritePortlets(
                this.user, this.portletDefinition1, this.portletDefinition3);
        doReturn(true)
                .when(spyPortletRestController)
                .doesUserHavePermissionToViewPortlet(any(), any(), eq(MANAGE));
        final ModelAndView mav = spyPortletRestController.getPortlets(request);
        this.verifyPortletResults(mav, this.portletDefinition1, this.portletDefinition3);
    }

    @Test
    public void testGetPortletsWhenLimitingToNonFavoritePortlets() {
        this.givenRequestSpecifiesFavoriteFlag(false);

        this.givenUserHasPermissionForPortlets(
                this.user,
                MANAGE,
                this.portletDefinition1,
                this.portletDefinition2,
                this.portletDefinition3);

        this.givenUserHasFavoritePortlets(this.user, this.portletDefinition1);

        doReturn(true)
                .when(spyPortletRestController)
                .doesUserHavePermissionToViewPortlet(any(), any(), eq(MANAGE));
        final ModelAndView mav = spyPortletRestController.getPortlets(request);
        this.verifyPortletResults(mav, this.portletDefinition2, this.portletDefinition3);
    }

    private void setupMockPortletDefinition(
            IPortletDefinition portletDefinition, IPortletDefinitionId id) {
        final String stringId = id.getStringId();
        given(portletDefinition.getFName()).willReturn("fname" + stringId);
        given(portletDefinition.getName()).willReturn("name" + stringId);
        given(portletDefinition.getDescription()).willReturn("description" + stringId);
        given(portletDefinition.getType()).willReturn(this.portletType);
        given(portletDefinition.getLifecycleState()).willReturn(PortletLifecycleState.PUBLISHED);
        given(portletDefinition.getPortletDefinitionId()).willReturn(id);
    }

    private void givenRequestSpecifiesPermissionType(
            IAuthorizationService.PortletPermissionType permissionType) {
        given(this.request.getParameter(PortletsRESTController.REQUIRED_PERMISSION_TYPE))
                .willReturn(permissionType.toString());
    }

    private void givenRequestSpecifiesFavoriteFlag(boolean flagValue) {
        given(this.request.getParameter(PortletsRESTController.FAVORITE_FLAG))
                .willReturn(flagValue ? "true" : "false");
    }

    private void givenPortletDefinitionsInRegistry(IPortletDefinition... portletDefinitions) {
        portletsFromRegistry.addAll(Arrays.asList(portletDefinitions));
    }

    private void givenUserHasPermissionForPortlets(
            IPerson user,
            IAuthorizationService.PortletPermissionType permissionType,
            IPortletDefinition... portletDefinitions) {
        for (IPortletDefinition portletDefinition : portletDefinitions) {
            final String portletDefinitionStringId =
                    portletDefinition.getPortletDefinitionId().getStringId();
            switch (permissionType) {
                case BROWSE:
                    this.authorizationService.canPrincipalBrowse(
                            this.authorizationPrincipal, portletDefinition);
                    break;
                case CONFIGURE:
                    this.authorizationService.canPrincipalConfigure(
                            this.authorizationPrincipal, portletDefinitionStringId);
                    break;
                case MANAGE:
                    this.authorizationService.canPrincipalManage(
                            this.authorizationPrincipal, portletDefinitionStringId);
                    break;
                case SUBSCRIBE:
                    this.authorizationService.canPrincipalSubscribe(
                            this.authorizationPrincipal, portletDefinitionStringId);
                    break;
                case RENDER:
                    this.authorizationService.canPrincipalRender(
                            this.authorizationPrincipal, portletDefinitionStringId);
                    break;
                default:
                    throw new IllegalArgumentException(
                            "Unknown permission type: " + permissionType);
            }
        }
    }

    private void givenUserHasFavoritePortlets(
            IPerson user, IPortletDefinition... portletDefinitions) {
        final Set<IPortletDefinition> favoritePortlets = new HashSet<>();
        for (IPortletDefinition portletDefinition : portletDefinitions) {
            favoritePortlets.add(portletDefinition);
        }
        given(this.userInstanceManager.getUserInstance(this.request)).willReturn(this.userInstance);
        given(this.userInstance.getPreferencesManager()).willReturn(this.userPreferencesManager);
        given(this.userPreferencesManager.getUserLayoutManager())
                .willReturn(this.userLayoutManager);
        given(this.userLayoutManager.getUserLayout()).willReturn(this.userLayout);
        given(this.favoritesUtils.getFavoritePortletDefinitions(this.userLayout))
                .willReturn(favoritePortlets);
    }

    private void verifyPortletResults(ModelAndView mav, IPortletDefinition... expectedPortlets) {
        assertNotNull(mav);
        assertEquals("json", mav.getViewName());
        final Map<String, Object> model = mav.getModel();
        assertNotNull(model);
        final List<PortletsRESTController.PortletTuple> portlets =
                (List<PortletsRESTController.PortletTuple>) model.get("portlets");
        assertNotNull(portlets);
        assertEquals(expectedPortlets.length, portlets.size());
        for (int i = 0; i < expectedPortlets.length; i++) {
            final IPortletDefinition expectedPortlet = expectedPortlets[i];
            final PortletsRESTController.PortletTuple portletTuple = portlets.get(i);
            assertEquals(expectedPortlet.getFName(), portletTuple.getFname());
            assertEquals(expectedPortlet.getName(), portletTuple.getName());
            assertEquals(expectedPortlet.getDescription(), portletTuple.getDescription());
            assertEquals(expectedPortlet.getType().getName(), portletTuple.getType());
            assertEquals(
                    expectedPortlet.getLifecycleState().toString(),
                    portletTuple.getLifecycleState());
            assertEquals(
                    expectedPortlet.getPortletDefinitionId().getStringId(), portletTuple.getId());
        }
    }

    static class PortletDefinitionId implements IPortletDefinitionId {
        private long id;

        public PortletDefinitionId(long id) {
            this.id = id;
        }

        @Override
        public long getLongId() {
            return id;
        }

        @Override
        public String getStringId() {
            return String.valueOf(id);
        }
    }
}
