/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.rest;

import com.google.common.collect.ImmutableSet;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apereo.portal.portlet.dao.IMarketplaceRatingDao;
import org.apereo.portal.portlet.dao.jpa.PortletDefinitionImpl;
import org.apereo.portal.portlet.dao.jpa.PortletTypeImpl;
import org.apereo.portal.portlet.marketplace.IMarketplaceService;
import org.apereo.portal.portlet.om.IPortletType;
import org.apereo.portal.rest.layout.MarketplaceEntry;
import org.apereo.portal.rest.layout.MarketplaceEntryRating;
import org.apereo.portal.security.IPerson;
import org.apereo.portal.security.IPersonManager;
import org.apereo.portal.security.provider.PersonImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.servlet.ModelAndView;

public class MarketplaceRESTControllerTest {
    public static final String USER_NAME = "jdoe";
    public static final String F_NAME = "john";
    @InjectMocks private MarketplaceRESTController marketplaceRESTController;

    @Mock private IPersonManager personManager;

    @Mock private HttpServletRequest req;

    @Mock private IMarketplaceService marketplaceService;

    @Mock private IMarketplaceRatingDao marketplaceRatingDAO;

    @Mock private HttpServletResponse res;

    @Before
    public void setup() throws Exception {
        marketplaceRESTController = new MarketplaceRESTController();
        req = new MockHttpServletRequest();
        res = new MockHttpServletResponse();
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void testGetUserRatingNULL() {

        String remoteUser = "jdoe";
        Mockito.when(req.getRemoteUser()).thenReturn("jdoe");
        IPortletType portletType =
                new PortletTypeImpl("John Doe", "http://localhost:8080/uportal/test");
        portletType.setDescription("Portlet Type");

        PortletDefinitionImpl tempPortlet =
                new PortletDefinitionImpl(
                        portletType, "John", "Doe", "Course", "app-id", "Courses", true);

        Mockito.when(
                        marketplaceService.getOrCreateMarketplacePortletDefinitionIfTheFnameExists(
                                remoteUser))
                .thenReturn(null);
        Mockito.when(marketplaceRatingDAO.getRating(remoteUser, tempPortlet)).thenReturn(null);
        ModelAndView modelAndView = marketplaceRESTController.getUserRating(req, F_NAME);
        Assert.assertNull(modelAndView.getModel().get("rating"));
    }

    @Test
    public void testGetUserRating() {

        String remoteUser = "jdoe";
        Mockito.when(req.getRemoteUser()).thenReturn("jdoe");
        IPortletType portletType =
                new PortletTypeImpl("John Doe", "http://localhost:8080/uportal/test");
        portletType.setDescription("Portlet Type");

        PortletDefinitionImpl tempPortlet =
                new PortletDefinitionImpl(
                        portletType, "John", "Doe", "Course", "app-id", "Courses", true);
        Mockito.when(
                        marketplaceService.getOrCreateMarketplacePortletDefinitionIfTheFnameExists(
                                remoteUser))
                .thenReturn(null);

        ModelAndView modelAndView = marketplaceRESTController.getUserRating(req, F_NAME);
        Assert.assertNull(modelAndView.getModel().get("rating"));
    }

    @Test
    public void testAllPortletRatingDaysBack() {

        String remoteUser = "jdoe";
        Mockito.when(req.getRemoteUser()).thenReturn("jdoe");
        IPortletType portletType =
                new PortletTypeImpl("John Doe", "http://localhost:8080/uportal/test");
        portletType.setDescription("Portlet Type");

        PortletDefinitionImpl tempPortlet =
                new PortletDefinitionImpl(
                        portletType, "John", "Doe", "Course", "app-id", "Courses", true);

        Mockito.when(
                        marketplaceService.getOrCreateMarketplacePortletDefinitionIfTheFnameExists(
                                remoteUser))
                .thenReturn(null);

        ModelAndView modelAndView = marketplaceRESTController.getPortletRatings(req, F_NAME, 30);
        Assert.assertNull(modelAndView.getModel().get("rating"));
    }

    @Test
    public void testSaveUserRating() {
        String rating = "3";
        String review = "Good job";
        Mockito.when(req.getRemoteUser()).thenReturn("jdoe");

        marketplaceRESTController.saveUserRating(req, F_NAME, rating, review);
        Mockito.verify(marketplaceService)
                .getOrCreateMarketplacePortletDefinitionIfTheFnameExists(F_NAME);
        Mockito.verify(marketplaceRatingDAO)
                .createOrUpdateRating(
                        Integer.parseInt(rating),
                        req.getRemoteUser(),
                        review,
                        marketplaceService.getOrCreateMarketplacePortletDefinitionIfTheFnameExists(
                                F_NAME));
    }

    @Test
    public void testSaveUserRatingNull() {
        String rating = "3";
        String review = "Good job";

        Mockito.when(req.getRemoteUser()).thenReturn("jdoe");

        ModelAndView modelAndView =
                marketplaceRESTController.saveUserRating(req, F_NAME, rating, review);
        MarketplaceEntryRating entry =
                (MarketplaceEntryRating) modelAndView.getModel().get("rating");
        Assert.assertNotNull(entry);
    }

    @Test
    public void testMarketplaceEntriesFeedNull() {
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);
        person.setFullName("john doe");

        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        Mockito.when(
                        marketplaceService.browseableMarketplaceEntriesFor(
                                person, Collections.emptySet()))
                .thenReturn(null);

        ModelAndView modelAndView = marketplaceRESTController.marketplaceEntriesFeed(req);
        Assert.assertNull(modelAndView.getModel().get("portlets"));
    }

    @Test
    public void testMarketplaceEntriesFeed() {
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);
        person.setFullName("john doe");
        Mockito.when(personManager.getPerson(req)).thenReturn(person);
        MarketplaceEntry entry = new MarketplaceEntry(null, null, person);

        Set<MarketplaceEntry> marketplaceEntries = new HashSet<MarketplaceEntry>();
        marketplaceEntries.add(entry);
        ImmutableSet<MarketplaceEntry> entries = ImmutableSet.copyOf(marketplaceEntries);
        Mockito.when(
                        marketplaceService.browseableMarketplaceEntriesFor(
                                person, Collections.emptySet()))
                .thenReturn(entries);
        ModelAndView modelAndView = marketplaceRESTController.marketplaceEntriesFeed(req);

        Set<MarketplaceEntry> returnEntries =
                (Set<MarketplaceEntry>) modelAndView.getModel().get("portlets");
        Assert.assertEquals(1L, entries.size());
    }

    @Test
    public void testMarketplaceEntriesFeedNoContent() {
        IPerson person = new PersonImpl();
        person.setUserName(USER_NAME);
        person.setFullName("john doe");
        Mockito.when(personManager.getPerson(req)).thenReturn(person);

        Mockito.when(
                        marketplaceService.browseableMarketplaceEntriesFor(
                                person, Collections.emptySet()))
                .thenReturn(null);
        ModelAndView modelAndView = marketplaceRESTController.marketplaceEntriesFeed(req);

        Set<MarketplaceEntry> returnEntries =
                (Set<MarketplaceEntry>) modelAndView.getModel().get("portlets");
        Assert.assertNull(returnEntries);
    }
}
