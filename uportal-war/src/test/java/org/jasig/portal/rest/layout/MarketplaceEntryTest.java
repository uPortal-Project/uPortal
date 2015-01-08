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
package org.jasig.portal.rest.layout;

import org.jasig.portal.portlet.marketplace.MarketplacePortletDefinition;
import org.jasig.portal.security.IPerson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import static org.junit.Assert.*;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for MarketplaceEntry.
 *
 * @since uPortal 4.2
 */
public class MarketplaceEntryTest {

    @Mock MarketplacePortletDefinition marketplacePortletDefinition;
    @Mock MarketplacePortletDefinition anotherMarketplacePortletDefinition;

    @Mock IPerson user;
    @Mock IPerson anotherUser;


    /**
     * Set up the test case.
     */
    @Before
    public void setUp() {
        initMocks(this);
    }

    /**
     * A MarketplaceEntry does not equals(null), of course.
     */
    @Test
    public void testDoesNotEqualNull() {

        final MarketplaceEntry anEntry = new MarketplaceEntry(marketplacePortletDefinition, user);

        assertFalse(anEntry.equals(null));
    }


    /**
     * MarketplaceEntries do not equal one another when their users differ.
     */
    @Test
    public void testDoesNotEqualWhenUsersDiffer() {

        final MarketplaceEntry anEntry = new MarketplaceEntry(marketplacePortletDefinition, user);

        final MarketplaceEntry sameDefinitionDifferentUser =
            new MarketplaceEntry(marketplacePortletDefinition, anotherUser);

        assertFalse(anEntry.equals(sameDefinitionDifferentUser));
    }

    /**
     * Marketplace entries do not equal one another when their backing portlet definitions differ.
     */
    @Test
    public void testDoesNotEqualWhenDefinitionsDiffer() {

        final MarketplaceEntry anEntry = new MarketplaceEntry(marketplacePortletDefinition, user);

        final MarketplaceEntry differentDefinitionSameUser =
            new MarketplaceEntry(anotherMarketplacePortletDefinition, user);

        assertFalse(anEntry.equals(differentDefinitionSameUser));

    }

    /**
     * Marketplace entries do not equal one another when their canAdd differs.
     */
    @Test
    public void testDoesNotEqualWhenCanAddDiffers() {

        final MarketplaceEntry anEntry = new MarketplaceEntry(marketplacePortletDefinition, user);

        final MarketplaceEntry sameDefinitionSameUser =
            new MarketplaceEntry(marketplacePortletDefinition, user);

        anEntry.setCanAdd(false);
        sameDefinitionSameUser.setCanAdd(true);

        assertFalse(anEntry.equals(sameDefinitionSameUser));

        anEntry.setCanAdd(true);
        sameDefinitionSameUser.setCanAdd(false);

        assertFalse(anEntry.equals(sameDefinitionSameUser));

        sameDefinitionSameUser.setCanAdd(true);
    }

    /**
     * Marketplace entries differ when their configuration as to whether to have related portlets
     * differs.
     */
    @Test
    public void testDoesNotEqualWhenHavingnessOfRelatedPortletsDiffers() {

        final MarketplaceEntry noRelatedPortlets =
            new MarketplaceEntry(marketplacePortletDefinition, false, user);
        noRelatedPortlets.setCanAdd(false);

        final MarketplaceEntry yesRelatedPortlets =
            new MarketplaceEntry(marketplacePortletDefinition, true, user);
        yesRelatedPortlets.setCanAdd(false);

        assertFalse(noRelatedPortlets.equals(yesRelatedPortlets));
    }

    /**
     * Test that Marketplace entries that have the same backing definition, the same backing user,
     * convey the same user permission to add, and have the same configuration as to whether to have
     * related portlets, are equal.
     */
    @Test
    public void testEquals() {

        final MarketplaceEntry anEntry =
            new MarketplaceEntry(marketplacePortletDefinition, true, user);

        final MarketplaceEntry equalEntry =
            new MarketplaceEntry(marketplacePortletDefinition, true, user);

        anEntry.setCanAdd(true);
        equalEntry.setCanAdd(true);

        assertEquals(anEntry, equalEntry);
        assertEquals(anEntry.hashCode(), equalEntry.hashCode());

        anEntry.setCanAdd(false);
        equalEntry.setCanAdd(false);

        assertEquals(anEntry, equalEntry);
        assertEquals(anEntry.hashCode(), equalEntry.hashCode());
    }

}
