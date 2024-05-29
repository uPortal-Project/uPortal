/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout.dlm.remoting;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Vector;
import javax.portlet.WindowState;
import org.apereo.portal.IUserIdentityStore;
import org.apereo.portal.UserInstance;
import org.apereo.portal.UserPreferencesManager;
import org.apereo.portal.groups.IEntity;
import org.apereo.portal.layout.IStylesheetUserPreferencesService;
import org.apereo.portal.layout.IUserLayout;
import org.apereo.portal.layout.IUserLayoutManager;
import org.apereo.portal.layout.IUserLayoutStore;
import org.apereo.portal.layout.dlm.MissingPortletDefinition;
import org.apereo.portal.layout.node.IUserLayoutChannelDescription;
import org.apereo.portal.layout.node.IUserLayoutFolderDescription;
import org.apereo.portal.layout.node.IUserLayoutNodeDescription;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletDefinitionId;
import org.apereo.portal.portlet.om.IPortletType;
import org.apereo.portal.portlet.registry.IPortletDefinitionRegistry;
import org.apereo.portal.portlet.registry.IPortletWindowRegistry;
import org.apereo.portal.portlet.registry.PortletDefinitionRegistryImpl;
import org.apereo.portal.portlets.favorites.FavoritesUtils;
import org.apereo.portal.security.IAuthorizationPrincipal;
import org.apereo.portal.security.IAuthorizationService;
import org.apereo.portal.security.IPermission;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.ISecurityContext;
import org.apereo.portal.security.provider.PersonImpl;
import org.apereo.portal.user.IUserInstance;
import org.apereo.portal.user.IUserInstanceManager;
import org.apereo.portal.utils.personalize.IPersonalizer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class UpdatePreferencesServletTest {
    @InjectMocks UpdatePreferencesServlet updatePreferencesServlet;
    @Mock private IAuthorizationService authorizationService;
    @Mock private IPortletDefinitionRegistry portletDefinitionRegistry;
    @Mock private IUserIdentityStore userIdentityStore;
    @Mock private IUserInstanceManager userInstanceManager;
    @Mock private IStylesheetUserPreferencesService stylesheetUserPreferencesService;
    @Mock private IUserLayoutStore userLayoutStore;
    @Mock private MessageSource messageSource;
    @Mock private IPortletWindowRegistry portletWindowRegistry;
    @Mock private WindowState addedWindowState;
    @Mock private IPersonalizer personalizer;
    @Mock protected final Logger logger = LoggerFactory.getLogger(getClass());
    @Mock protected ISecurityContext m_securityContext = null;
    @Mock IPerson person;
    @Mock IEntity personEntity;
    @Mock IUserInstance userInstance;
    @Mock IPortletType portletType;
    @Mock IPortletDefinitionId portletDefinitionId;
    @Mock IPortletDefinition portletDefForPortletBeingFavorited;
    @Mock IPortletDefinition portletDefForFavoritedPortlet1;
    @Mock IPortletDefinition portletDefForFavoritedPortlet2;
    @Mock IPortletDefinition portletDefForFavoritedPortlet3;
    @Mock IAuthorizationPrincipal authPrincipal;
    @Mock UserPreferencesManager upm;
    @Mock IUserLayoutManager ulm;
    @Mock IUserLayout userLayout;
    @Mock IUserLayoutFolderDescription favoritesFolderNodeDescription;
    @Mock IUserLayoutChannelDescription favoritedPortlet1ChannelDescription;
    @Mock IUserLayoutChannelDescription favoritedPortlet2ChannelDescription;
    @Mock IUserLayoutChannelDescription favoritedPortlet3ChannelDescription;
    @Mock IUserLayoutChannelDescription newFavoritedPortletChannelDescription;
    @Mock IUserLayoutChannelDescription orphanedFavoritedPortletChannelDescription;

    private AutoCloseable closeable;
    private MockHttpServletRequest req;
    private MockHttpServletResponse res;
    private final String favoritedPortlet1Id = "111";
    private final String favoritedPortlet2Id = "222";
    private final String favoritedPortlet3Id = "333";
    private final String favoritedPortlet1Fname = "fname-" + favoritedPortlet1Id;
    private final String favoritedPortlet2Fname = "fname-" + favoritedPortlet2Id;
    private final String favoritedPortlet3Fname = "fname-" + favoritedPortlet3Id;
    private final String getFavoritedPortlet1SubscribeId = "1111";
    private final String getFavoritedPortlet2SubscribeId = "2222";
    private final String getFavoritedPortlet3SubscribeId = "3333";
    private final String orphanedFavoritedPortletSubscribeId = "123";
    private final String favoritesFolderNodeId = "7";
    private final String favoritesFolderColumnNodeId = "77";
    private final String idForPortletBeingFavorited = "777";
    private final String userLayoutRootId = "1";
    private FavoritesUtils favoritesUtils;

    @Before
    public void setup() {
        updatePreferencesServlet = new UpdatePreferencesServlet();
        res = new MockHttpServletResponse();
        req = new MockHttpServletRequest();
        closeable = MockitoAnnotations.openMocks(this);
        favoritesUtils = new FavoritesUtils();
        favoritesUtils.setPortletDefinitionRegistry(portletDefinitionRegistry);
        updatePreferencesServlet.setFavoritesUtils(favoritesUtils);
        when(favoritesFolderNodeDescription.getName()).thenReturn("Favorites");
        when(favoritesFolderNodeDescription.getType())
                .thenReturn(IUserLayoutNodeDescription.LayoutNodeType.FOLDER);
        when(((IUserLayoutFolderDescription) favoritesFolderNodeDescription).getFolderType())
                .thenReturn("favorites");
        when(favoritesFolderNodeDescription.getId()).thenReturn(favoritesFolderNodeId);
        when(userLayout.getNodeDescription(favoritesFolderNodeId))
                .thenReturn(favoritesFolderNodeDescription);
        when(userInstanceManager.getUserInstance(req)).thenReturn(userInstance);
        when(userInstance.getPerson()).thenReturn(person);
        when(portletDefinitionId.getStringId()).thenReturn(idForPortletBeingFavorited);
        when(portletDefForPortletBeingFavorited.getPortletDefinitionId())
                .thenReturn(portletDefinitionId);
        when(portletDefinitionRegistry.getPortletDefinition(idForPortletBeingFavorited))
                .thenReturn(portletDefForPortletBeingFavorited);
        when(portletDefForPortletBeingFavorited.getType()).thenReturn(portletType);
        when(authorizationService.newPrincipal(any())).thenReturn(authPrincipal);
        when(authPrincipal.hasPermission(
                        eq(IPermission.PORTAL_SYSTEM),
                        eq(IPermission.PORTLET_FAVORITE_ACTIVITY),
                        any()))
                .thenReturn(true);
        when(userInstance.getPreferencesManager()).thenReturn(upm);
        when(upm.getUserLayoutManager()).thenReturn(ulm);
        when(ulm.getUserLayout()).thenReturn(userLayout);
        when(userLayout.getRootId()).thenReturn(userLayoutRootId);
        when(favoritedPortlet1ChannelDescription.getChannelPublishId())
                .thenReturn(favoritedPortlet1Id);
        when(favoritedPortlet2ChannelDescription.getChannelPublishId())
                .thenReturn(favoritedPortlet2Id);
        when(favoritedPortlet3ChannelDescription.getChannelPublishId())
                .thenReturn(favoritedPortlet3Id);
        when(orphanedFavoritedPortletChannelDescription.getChannelPublishId())
                .thenReturn(MissingPortletDefinition.CHANNEL_ID);
        when(favoritedPortlet1ChannelDescription.getFunctionalName())
                .thenReturn(favoritedPortlet1Fname);
        when(favoritedPortlet2ChannelDescription.getFunctionalName())
                .thenReturn(favoritedPortlet2Fname);
        when(favoritedPortlet3ChannelDescription.getFunctionalName())
                .thenReturn(favoritedPortlet3Fname);
        when(orphanedFavoritedPortletChannelDescription.getFunctionalName())
                .thenReturn(MissingPortletDefinition.FNAME);
        when(favoritedPortlet1ChannelDescription.getChannelSubscribeId())
                .thenReturn(getFavoritedPortlet1SubscribeId);
        when(favoritedPortlet2ChannelDescription.getChannelSubscribeId())
                .thenReturn(getFavoritedPortlet2SubscribeId);
        when(favoritedPortlet3ChannelDescription.getChannelSubscribeId())
                .thenReturn(getFavoritedPortlet3SubscribeId);
        when(orphanedFavoritedPortletChannelDescription.getChannelSubscribeId())
                .thenReturn(orphanedFavoritedPortletSubscribeId);
        when(portletDefinitionRegistry.getPortletDefinition(favoritedPortlet1Id))
                .thenReturn(portletDefForFavoritedPortlet1);
        when(portletDefinitionRegistry.getPortletDefinition(favoritedPortlet2Id))
                .thenReturn(portletDefForFavoritedPortlet2);
        when(portletDefinitionRegistry.getPortletDefinition(favoritedPortlet3Id))
                .thenReturn(portletDefForFavoritedPortlet3);
        when(portletDefForFavoritedPortlet1.getType()).thenReturn(portletType);
        when(portletDefForFavoritedPortlet2.getType()).thenReturn(portletType);
        when(portletDefForFavoritedPortlet3.getType()).thenReturn(portletType);
        when(portletDefForFavoritedPortlet1.getPortletDefinitionId())
                .thenReturn(portletDefinitionId);
        when(portletDefForFavoritedPortlet2.getPortletDefinitionId())
                .thenReturn(portletDefinitionId);
        when(portletDefForFavoritedPortlet3.getPortletDefinitionId())
                .thenReturn(portletDefinitionId);
        when(portletDefForFavoritedPortlet1.getFName()).thenReturn(favoritedPortlet1Fname);
        when(portletDefForFavoritedPortlet2.getFName()).thenReturn(favoritedPortlet2Fname);
        when(portletDefForFavoritedPortlet3.getFName()).thenReturn(favoritedPortlet3Fname);
        when(userLayout.getNodeDescription(favoritedPortlet1Id))
                .thenReturn(favoritedPortlet1ChannelDescription);
        when(userLayout.getNodeDescription(favoritedPortlet2Id))
                .thenReturn(favoritedPortlet2ChannelDescription);
        when(userLayout.getNodeDescription(favoritedPortlet3Id))
                .thenReturn(favoritedPortlet3ChannelDescription);
        when(userLayout.getNodeDescription(MissingPortletDefinition.CHANNEL_ID))
                .thenReturn(orphanedFavoritedPortletChannelDescription);
        when(ulm.addNode(any(), eq(favoritesFolderColumnNodeId), eq((String) null)))
                .thenReturn(newFavoritedPortletChannelDescription);
        when(ulm.deleteNode(any())).thenReturn(true);
        when(newFavoritedPortletChannelDescription.getId()).thenReturn(idForPortletBeingFavorited);
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveElement() throws IOException {
        when(userInstanceManager.getUserInstance(req)).thenReturn(null);
        updatePreferencesServlet.removeElement(req, res);
    }

    @After
    public void tearDown() throws Exception {
        closeable.close();
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveByFNameNullForUserInstanceNotFound() throws IOException {
        when(userInstanceManager.getUserInstance(req)).thenReturn(null);
        updatePreferencesServlet.removeByFName(req, res, "fname");
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveByFName() throws IOException {
        IPerson person = new PersonImpl();
        person.setUserName("jDoe");
        person.setFullName("john doe");
        IUserInstance userInstance = new UserInstance(person, null, null);

        when(userInstanceManager.getUserInstance(req)).thenReturn(userInstance);
        updatePreferencesServlet.removeByFName(req, res, "fname");
    }

    @Test(expected = NullPointerException.class)
    public void testMovePortletAjax() {
        req.setLocalName("en-US");
        updatePreferencesServlet.movePortletAjax(req, res, "sourceId", "prevNodeIs", "nextNodeId");
    }

    @Test(expected = NullPointerException.class)
    public void testMoveElement() throws IOException {
        req.setLocalName("en-US");
        when(userInstanceManager.getUserInstance(req)).thenReturn(null);
        updatePreferencesServlet.moveElement(req, res, "sourceId", "get", "elementId");
    }

    @Test(expected = IllegalArgumentException.class)
    public void testAddFavoriteWithNullPortletId() throws IOException {
        updatePreferencesServlet.setPortletDefinitionRegistry(new PortletDefinitionRegistryImpl());
        updatePreferencesServlet.setPersonEntityService(
                this.createPersonEntityService(personEntity));
        updatePreferencesServlet.addFavorite(null, req, res);
    }

    @Test
    public void testAddFavoriteSucceeds() throws IOException {
        req.setLocalName("en-US");
        updatePreferencesServlet.setPersonEntityService(
                this.createPersonEntityService(personEntity));
        whenFavoritesFolderColumnNodeIsPresent();
        whenFavoritedPortletsArePresent(
                favoritedPortlet1Id, favoritedPortlet2Id, favoritedPortlet3Id);
        ModelAndView mav =
                updatePreferencesServlet.addFavorite(idForPortletBeingFavorited, req, res);
        assertNotNull(mav);
        assertEquals("jsonView", mav.getViewName());
        assertEquals(idForPortletBeingFavorited, mav.getModel().get("newNodeId"));
        verify(ulm).addNode(any(), eq(favoritesFolderColumnNodeId), eq((String) null));
        verify(ulm).saveUserLayout();
    }

    @Test
    public void testAddFavoriteRemovesFavoriteForPortletThatHasBeenDeleted() throws IOException {
        req.setLocalName("en-US");
        updatePreferencesServlet.setPersonEntityService(
                this.createPersonEntityService(personEntity));
        whenFavoritesFolderColumnNodeIsPresent();
        whenFavoritedPortletsArePresent(
                favoritedPortlet1Id, MissingPortletDefinition.CHANNEL_ID, favoritedPortlet3Id);
        ModelAndView mav =
                updatePreferencesServlet.addFavorite(idForPortletBeingFavorited, req, res);
        assertNotNull(mav);
        assertEquals("jsonView", mav.getViewName());
        assertEquals(idForPortletBeingFavorited, mav.getModel().get("newNodeId"));
        verify(ulm).deleteNode(orphanedFavoritedPortletSubscribeId);
        verify(ulm).addNode(any(), eq(favoritesFolderColumnNodeId), eq((String) null));
        verify(ulm).saveUserLayout();
    }

    @Test
    public void testRemoveFavoriteSucceeds() throws IOException {
        req.setLocalName("en-US");
        updatePreferencesServlet.setPersonEntityService(
                this.createPersonEntityService(personEntity));
        whenFavoritesFolderColumnNodeIsPresent();
        whenFavoritedPortletsArePresent(
                favoritedPortlet1Id, favoritedPortlet2Id, favoritedPortlet3Id);
        ModelAndView mav = updatePreferencesServlet.removeFavorite(favoritedPortlet2Id, req, res);
        assertNotNull(mav);
        assertEquals("jsonView", mav.getViewName());
        verify(ulm).deleteNode(getFavoritedPortlet2SubscribeId);
        verify(ulm).saveUserLayout();
        verify(messageSource).getMessage(eq("success.remove.portlet"), any(), any(), any());
    }

    @Test(expected = NullPointerException.class)
    public void testRemoveFavorite() throws IOException {
        req.setLocalName("en-US");
        IPerson person = new PersonImpl();
        person.setUserName("jdoe");
        person.setFullName("john doe");
        IUserInstance userInstance = new UserInstance(person, null, null);
        when(userInstanceManager.getUserInstance(req)).thenReturn(userInstance);
        updatePreferencesServlet.removeFavorite("channelId", req, res);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testRemoveFavoriteWithNullPortletId() throws IOException {
        updatePreferencesServlet.setPortletDefinitionRegistry(new PortletDefinitionRegistryImpl());
        updatePreferencesServlet.setPersonEntityService(
                this.createPersonEntityService(personEntity));
        updatePreferencesServlet.removeFavorite(null, req, res);
    }

    @Test
    public void testRemoveFavoriteRemovesFavoriteForPortletThatHasBeenDeleted() throws IOException {
        req.setLocalName("en-US");
        updatePreferencesServlet.setPersonEntityService(
                this.createPersonEntityService(personEntity));
        whenFavoritesFolderColumnNodeIsPresent();
        whenFavoritedPortletsArePresent(
                favoritedPortlet1Id, MissingPortletDefinition.CHANNEL_ID, favoritedPortlet3Id);
        ModelAndView mav = updatePreferencesServlet.removeFavorite(favoritedPortlet3Id, req, res);
        assertNotNull(mav);
        assertEquals("jsonView", mav.getViewName());
        verify(ulm).deleteNode(orphanedFavoritedPortletSubscribeId);
        verify(ulm).deleteNode(getFavoritedPortlet3SubscribeId);
        verify(ulm).saveUserLayout();
        verify(messageSource).getMessage(eq("success.remove.portlet"), any(), any(), any());
    }

    private UpdatePreferencesServlet.IPersonEntityService createPersonEntityService(
            IEntity personEntity) {
        return new UpdatePreferencesServlet.IPersonEntityService() {
            @Override
            public IEntity getPersonEntity(String username) {
                return personEntity;
            }
        };
    }

    private Enumeration<String> createNodeIdsEnumeration(String... nodeIds) {
        return new Vector<String>(new HashSet<String>(Arrays.asList(nodeIds))).elements();
    }

    private void whenFavoritesFolderColumnNodeIsPresent() {
        when(userLayout.getChildIds(userLayoutRootId))
                .thenReturn(this.createNodeIdsEnumeration(favoritesFolderNodeId))
                .thenReturn(this.createNodeIdsEnumeration(favoritesFolderNodeId))
                .thenReturn(this.createNodeIdsEnumeration(favoritesFolderNodeId));
        when(userLayout.getChildIds(favoritesFolderNodeId))
                .thenReturn(this.createNodeIdsEnumeration(favoritesFolderColumnNodeId))
                .thenReturn(this.createNodeIdsEnumeration(favoritesFolderColumnNodeId))
                .thenReturn(this.createNodeIdsEnumeration(favoritesFolderColumnNodeId));
        when(ulm.getChildIds(favoritesFolderNodeId))
                .thenReturn(this.createNodeIdsEnumeration(favoritesFolderColumnNodeId))
                .thenReturn(this.createNodeIdsEnumeration(favoritesFolderColumnNodeId))
                .thenReturn(this.createNodeIdsEnumeration(favoritesFolderColumnNodeId));
    }

    private void whenFavoritedPortletsArePresent(String... favoritedPortletIds) {
        when(userLayout.getChildIds(favoritesFolderColumnNodeId))
                .thenReturn(this.createNodeIdsEnumeration(favoritedPortletIds));
    }
}
