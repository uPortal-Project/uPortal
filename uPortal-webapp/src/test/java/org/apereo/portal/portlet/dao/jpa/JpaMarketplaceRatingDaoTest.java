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
package org.apereo.portal.portlet.dao.jpa;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import org.apereo.portal.persondir.ILocalAccountDao;
import org.apereo.portal.persondir.ILocalAccountPerson;
import org.apereo.portal.portlet.dao.IMarketplaceRatingDao;
import org.apereo.portal.portlet.dao.IPortletDefinitionDao;
import org.apereo.portal.portlet.dao.IPortletTypeDao;
import org.apereo.portal.portlet.marketplace.IMarketplaceRating;
import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.om.IPortletType;
import org.apereo.portal.test.BasePortalJpaDaoTest;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:jpaPortalTestApplicationContext.xml")
@ComponentScan("org.apereo.portal.portlet.marketplace")
public class JpaMarketplaceRatingDaoTest extends BasePortalJpaDaoTest {

    @Autowired private IPortletDefinitionDao portletDefinitionDao;

    @Autowired private IMarketplaceRatingDao marketplaceRatingDao;

    @Autowired private ILocalAccountDao localAccountDao;

    @Autowired private IPortletTypeDao jpaChannelTypeDao;

    private final Random random = new Random();

    private final int TEST_DATE_INCREMENT = 20;

    @Before
    public void setup() {
        List<ILocalAccountPerson> personList = localAccountDao.getAllAccounts();
        // Just a quick assertion that this is utilizing the correct db
        assertEquals(personList.size(), 0);
        /*
         * Let's make two users (user1, user2)
         * We want to make sure that this finishes before moving on
         * This assumes that localAccountDao is correct and we
         * won't retest that code here, but rather in
         * @link org.apereo.portal.persondir.dao.jpa.JpaLocalAccountDaoImplTest
         */
        this.execute(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        final ILocalAccountPerson user1 = localAccountDao.createPerson("user1");
                        ;
                        localAccountDao.updateAccount(user1);

                        final ILocalAccountPerson user2 = localAccountDao.createPerson("user2");
                        localAccountDao.updateAccount(user2);
                        return null;
                    }
                });

        /*
         * Let's make up some portlets
         * This assumes the portletDefinitionDao works
         * won't retest that code here but rather in
         * @link org.apereo.portal.portlet.dao.jpa.JpaPortletDaoTest
         *
         */
        this.execute(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        List<IPortletDefinition> portletList =
                                portletDefinitionDao.getPortletDefinitions();
                        // Just a quick assertion that this is utilizing the correct db
                        assertEquals(portletList.size(), 0);
                        // Create portletType
                        final IPortletType channelType =
                                jpaChannelTypeDao.createPortletType("BaseType", "foobar");
                        // Create a definition
                        final IPortletDefinition portletDef1 =
                                new PortletDefinitionImpl(
                                        channelType,
                                        "fname1",
                                        "Test Portlet 1",
                                        "Test Portlet 1 Title",
                                        "/context1",
                                        "portletName1",
                                        false);
                        portletDefinitionDao.savePortletDefinition(portletDef1);
                        // Create a second definition with the same app/portlet
                        final IPortletDefinition portletDef2 =
                                new PortletDefinitionImpl(
                                        channelType,
                                        "fname2",
                                        "Test Portlet 2",
                                        "Test Portlet 2 Title",
                                        "/uPortal",
                                        "portletName2",
                                        true);
                        portletDefinitionDao.savePortletDefinition(portletDef2);
                        return null;
                    }
                });
    }

    @Test
    public void testCreateAndRetrieveRating() {
        // Easy Test Can we make every combination of portlet and user rating
        this.execute(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        List<IPortletDefinition> portletList =
                                portletDefinitionDao.getPortletDefinitions();
                        List<ILocalAccountPerson> personList = localAccountDao.getAllAccounts();
                        assertNotEquals(portletList.size(), 0);
                        assertNotEquals(personList.size(), 0);
                        for (IPortletDefinition portlet : portletList) {
                            for (ILocalAccountPerson person : personList) {
                                MarketplaceRatingImpl rating = new MarketplaceRatingImpl();
                                int starRating =
                                        random.nextInt(MarketplaceRatingImpl.MAX_RATING) + 1;
                                rating.setRating(starRating);
                                MarketplaceRatingPK ratingPK = new MarketplaceRatingPK();
                                ratingPK.setPortletDefinition((PortletDefinitionImpl) portlet);
                                ratingPK.setUserName(person.getName());
                                rating.setMarketplaceRatingPK(ratingPK);
                                rating.setRatingDate(new Date());
                                marketplaceRatingDao.createOrUpdateRating(rating);
                            }
                        }
                        return null;
                    }
                });
        // Now let's retrieve our objects and delete them
        this.execute(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        Set<IMarketplaceRating> ratingList = marketplaceRatingDao.getAllRatings();
                        assertNotEquals(ratingList.size(), 0);
                        for (IMarketplaceRating rating : ratingList) {
                            marketplaceRatingDao.deleteRating(rating);
                        }
                        assertEquals(marketplaceRatingDao.getAllRatings().size(), 0);
                        return null;
                    }
                });
    }

    @Test
    public void testUpdateRatings() {
        // Let's create some ratings
        this.execute(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        List<IPortletDefinition> portletList =
                                portletDefinitionDao.getPortletDefinitions();
                        List<ILocalAccountPerson> personList = localAccountDao.getAllAccounts();
                        assertNotEquals(portletList.size(), 0);
                        assertNotEquals(personList.size(), 0);
                        for (IPortletDefinition portlet : portletList) {
                            for (ILocalAccountPerson person : personList) {
                                MarketplaceRatingImpl rating = new MarketplaceRatingImpl();
                                int starRating =
                                        random.nextInt(MarketplaceRatingImpl.MAX_RATING) + 1;
                                rating.setRating(starRating);
                                MarketplaceRatingPK ratingPK = new MarketplaceRatingPK();
                                ratingPK.setPortletDefinition((PortletDefinitionImpl) portlet);
                                ratingPK.setUserName(person.getName());
                                rating.setMarketplaceRatingPK(ratingPK);
                                marketplaceRatingDao.createOrUpdateRating(rating);
                            }
                        }
                        return null;
                    }
                });
        // Now let's update with random numbers
        this.execute(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        List<IPortletDefinition> portletList =
                                portletDefinitionDao.getPortletDefinitions();
                        List<ILocalAccountPerson> personList = localAccountDao.getAllAccounts();
                        assertNotEquals(portletList.size(), 0);
                        assertNotEquals(personList.size(), 0);
                        for (IPortletDefinition portlet : portletList) {
                            for (ILocalAccountPerson person : personList) {
                                MarketplaceRatingImpl rating = new MarketplaceRatingImpl();
                                int starRating =
                                        random.nextInt(MarketplaceRatingImpl.MAX_RATING) + 1;
                                rating.setRating(starRating);
                                org.apereo.portal.portlet.dao.jpa.MarketplaceRatingPK ratingPK =
                                        new MarketplaceRatingPK();
                                ratingPK.setPortletDefinition((PortletDefinitionImpl) portlet);
                                ratingPK.setUserName(person.getName());
                                rating.setMarketplaceRatingPK(ratingPK);
                                rating.setRatingDate(new Date());
                                marketplaceRatingDao.createOrUpdateRating(rating);
                            }
                        }
                        return null;
                    }
                });
        // Now let's retrieve our objects and delete them
        this.execute(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        Set<IMarketplaceRating> ratingList = marketplaceRatingDao.getAllRatings();
                        assertNotEquals(ratingList.size(), 0);
                        for (IMarketplaceRating rating : ratingList) {
                            marketplaceRatingDao.deleteRating(rating);
                        }
                        assertEquals(marketplaceRatingDao.getAllRatings().size(), 0);
                        return null;
                    }
                });
    }

    @Test
    public void testAggregatingRatings() {
        this.execute(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        // first create some ratings
                        List<IPortletDefinition> portletList =
                                portletDefinitionDao.getPortletDefinitions();
                        List<ILocalAccountPerson> personList = localAccountDao.getAllAccounts();
                        assertNotEquals(portletList.size(), 0);
                        assertNotEquals(personList.size(), 0);
                        for (IPortletDefinition portlet : portletList) {
                            for (ILocalAccountPerson person : personList) {
                                MarketplaceRatingImpl rating = new MarketplaceRatingImpl();
                                int starRating =
                                        random.nextInt(MarketplaceRatingImpl.MAX_RATING) + 1;
                                rating.setRating(starRating);
                                MarketplaceRatingPK ratingPK = new MarketplaceRatingPK();
                                ratingPK.setPortletDefinition((PortletDefinitionImpl) portlet);
                                ratingPK.setUserName(person.getName());
                                rating.setMarketplaceRatingPK(ratingPK);
                                rating.setRatingDate(new Date());
                                marketplaceRatingDao.createOrUpdateRating(rating);
                            }
                        }
                        // now aggregate them
                        marketplaceRatingDao.aggregateMarketplaceRating();

                        // now verified the portlet definitions are up-to-date with aggregated data
                        List<IPortletDefinition> updatedPortletList =
                                portletDefinitionDao.getPortletDefinitions();
                        for (IPortletDefinition def : updatedPortletList) {
                            assertNotNull(def.getRating());
                            assertNotNull(def.getUsersRated());
                        }
                        return null;
                    }
                });
    }

    @Test
    public void testGetRatingsLastXDays() {
        // Easy Test Can we make every combination of portlet and user rating
        this.execute(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {

                        Calendar today = Calendar.getInstance();

                        List<IPortletDefinition> portletList =
                                portletDefinitionDao.getPortletDefinitions();
                        List<ILocalAccountPerson> personList = localAccountDao.getAllAccounts();
                        assertNotEquals(portletList.size(), 0);
                        assertNotEquals(personList.size(), 0);
                        int numberOfDaysBack = 1;
                        for (IPortletDefinition portlet : portletList) {

                            for (ILocalAccountPerson person : personList) {
                                today.add(Calendar.DAY_OF_YEAR, 0 - numberOfDaysBack);
                                MarketplaceRatingImpl rating = new MarketplaceRatingImpl();
                                int starRating =
                                        random.nextInt(MarketplaceRatingImpl.MAX_RATING) + 1;
                                rating.setRating(starRating);
                                MarketplaceRatingPK ratingPK = new MarketplaceRatingPK();
                                ratingPK.setPortletDefinition((PortletDefinitionImpl) portlet);
                                ratingPK.setUserName(person.getName());
                                rating.setMarketplaceRatingPK(ratingPK);
                                rating.setRatingDate(today.getTime());
                                rating.setReview(
                                        "comment for "
                                                + ratingPK.getPortletDefinition().getFName());
                                marketplaceRatingDao.createOrUpdateRating(rating);
                                numberOfDaysBack = numberOfDaysBack + TEST_DATE_INCREMENT;
                            }
                        }
                        return null;
                    }
                });
        // Now let's retrieve our objects
        this.execute(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {

                        int daysBack = 7;
                        Set<IMarketplaceRating> ratingList =
                                marketplaceRatingDao.getAllRatingsInLastXDays(daysBack);
                        assertEquals(ratingList.size(), 1);

                        daysBack = 30;
                        ratingList = marketplaceRatingDao.getAllRatingsInLastXDays(daysBack);
                        assertEquals(ratingList.size(), 2);

                        daysBack = 5000;
                        ratingList = marketplaceRatingDao.getAllRatingsInLastXDays(daysBack);
                        assertEquals(ratingList.size(), 4);

                        return null;
                    }
                });
    }

    @Test
    public void testGetAllRatingsForPortletInLastXDays() {
        // Let's create some ratings
        this.execute(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {

                        Calendar today = Calendar.getInstance();

                        List<IPortletDefinition> portletList =
                                portletDefinitionDao.getPortletDefinitions();
                        List<ILocalAccountPerson> personList = localAccountDao.getAllAccounts();
                        assertNotEquals(portletList.size(), 0);
                        assertNotEquals(personList.size(), 0);
                        int numberOfDaysBack = 1;
                        for (IPortletDefinition portlet : portletList) {

                            for (ILocalAccountPerson person : personList) {
                                today.add(Calendar.DAY_OF_YEAR, 0 - numberOfDaysBack);
                                System.out.println(
                                        "Testing get ratings using "
                                                + today.getTime()
                                                + " for portlet creation date. Porlet is "
                                                + portlet.getFName());
                                MarketplaceRatingImpl rating = new MarketplaceRatingImpl();
                                int starRating =
                                        random.nextInt(MarketplaceRatingImpl.MAX_RATING) + 1;
                                rating.setRating(starRating);
                                MarketplaceRatingPK ratingPK = new MarketplaceRatingPK();
                                ratingPK.setPortletDefinition((PortletDefinitionImpl) portlet);
                                ratingPK.setUserName(person.getName());
                                rating.setMarketplaceRatingPK(ratingPK);
                                rating.setRatingDate(today.getTime());
                                rating.setReview(
                                        "comment for "
                                                + ratingPK.getPortletDefinition().getFName());
                                marketplaceRatingDao.createOrUpdateRating(rating);
                                numberOfDaysBack = numberOfDaysBack + TEST_DATE_INCREMENT;
                            }
                        }
                        return null;
                    }
                });
        // Now let's test getting a rating for specific portlet and specific number of
        // days back
        this.execute(
                new Callable<Object>() {
                    @Override
                    public Object call() throws Exception {
                        int daysBack = 7;
                        Set<IMarketplaceRating> ratingList =
                                marketplaceRatingDao.getAllRatingsForPortletInLastXDays(
                                        daysBack, "fname1");
                        assertEquals(ratingList.size(), 1);

                        daysBack = 30;
                        ratingList =
                                marketplaceRatingDao.getAllRatingsForPortletInLastXDays(
                                        daysBack, "fname1");
                        assertEquals(ratingList.size(), 2);
                        return null;
                    }
                });
    }
}
