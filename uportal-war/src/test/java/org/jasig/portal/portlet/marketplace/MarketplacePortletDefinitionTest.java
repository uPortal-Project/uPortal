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
package org.jasig.portal.portlet.marketplace;

import com.google.common.collect.ImmutableSet;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.registry.IPortletCategoryRegistry;
import org.jasig.portal.security.IPerson;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for MarketplacePortletDefinition.
 */
public class MarketplacePortletDefinitionTest {

    @Mock IPortletDefinition portletDefinition;
    @Mock IPortletDefinition anotherPortletDefinition;

    @Mock IPortletCategoryRegistry categoryRegistry;

    @Mock IMarketplaceService marketplaceService;

    MarketplacePortletDefinition marketplacePortletDefinition;

    @Mock MarketplacePortletDefinition browseableDefinition1;
    @Mock MarketplacePortletDefinition browseableDefinition2;
    @Mock MarketplacePortletDefinition browseableRelatedDefinition1;
    @Mock MarketplacePortletDefinition browseableRelatedDefinition2;
    @Mock MarketplacePortletDefinition relatedDefinition1;
    @Mock MarketplacePortletDefinition relatedDefinition2;

    @Mock IPerson person;
    @Mock IPerson anotherPerson;

    @Before
    public void setUp() {
        initMocks(this);

        marketplacePortletDefinition =
            new MarketplacePortletDefinition(portletDefinition, person,
                marketplaceService, categoryRegistry);

        final Set<MarketplacePortletDefinition> allRelatedPortlets = new HashSet<>();
        allRelatedPortlets.add(this.browseableRelatedDefinition1);
        allRelatedPortlets.add(this.browseableRelatedDefinition2);
        allRelatedPortlets.add(this.relatedDefinition1);
        allRelatedPortlets.add(this.relatedDefinition2);

        when(marketplaceService.marketplacePortletDefinitionsRelatedTo
            (marketplacePortletDefinition)).thenReturn(ImmutableSet.copyOf(allRelatedPortlets));

        when(browseableRelatedDefinition1.getPerson()).thenReturn(person);
        when(browseableRelatedDefinition2.getPerson()).thenReturn(person);
    }

    /**
     * Test that a MarketplacePortletDefinition passes through the
     * alternativeMaximizedLink of the IPortletDefinition it wraps.
     */
    @Test
    public void testReflectsWrappedDefinitionAlternativeMaximizedLink() {

        when(portletDefinition.getAlternativeMaximizedLink()).thenReturn("http://apereo.org");

        assertEquals("http://apereo.org",
            marketplacePortletDefinition.getAlternativeMaximizedLink());
    }

    /**
     * Test that when there is an alternative maximized link, the render URL is that link.
     */
    @Test
    public void testRenderUrlIsAlternativeMaximizedLinkWhenPresent() {

        when(portletDefinition.getAlternativeMaximizedLink()).thenReturn("http://apereo.org");

        assertEquals("http://apereo.org",
            marketplacePortletDefinition.getRenderUrl());

    }

    /**
     * Test that when asked about related portlets, filters to browseable.
     */
    @Test
    public void testRelatedPortletsFiltersToBrowseable() {

        final Set<MarketplacePortletDefinition> allBrowseablePortlets = new HashSet<>();
        allBrowseablePortlets.add(this.browseableRelatedDefinition1);
        allBrowseablePortlets.add(this.browseableRelatedDefinition2);
        allBrowseablePortlets.add(this.browseableDefinition1);
        allBrowseablePortlets.add(this.browseableDefinition2);

        when(marketplaceService.marketplacePortletDefinitionsBrowseableBy(person))
            .thenReturn(ImmutableSet.copyOf(allBrowseablePortlets));

        final Set<MarketplacePortletDefinition> expectedBrowseableRelated = new HashSet<>();
        expectedBrowseableRelated.add(this.browseableRelatedDefinition1);
        expectedBrowseableRelated.add(this.browseableRelatedDefinition2);

        assertEquals(expectedBrowseableRelated,
            marketplacePortletDefinition.getRelatedPortlets());

    }

    /**
     * Test that related portlet definitions have the same user as that of the definition they
     * are read from.
     */
    @Test
    public void testRelatedPortletsInheritTheUser() {

        final Set<MarketplacePortletDefinition> allBrowseablePortlets = new HashSet<>();
        allBrowseablePortlets.add(this.browseableRelatedDefinition1);

        when(marketplaceService.marketplacePortletDefinitionsBrowseableBy(person))
            .thenReturn(ImmutableSet.copyOf(allBrowseablePortlets));

        final Set<MarketplacePortletDefinition> relatedPortlets =
            marketplacePortletDefinition.getRelatedPortlets();

        assertEquals(1, relatedPortlets.size());

        final MarketplacePortletDefinition relatedPortlet =
            (MarketplacePortletDefinition) relatedPortlets.toArray()[0];

        assertEquals(person, relatedPortlet.getPerson());

    }

    /**
     * Test that when asked about random related portlets, if underlying failure, that failure is
     * not propagated and instead manifests as empty set of related portlets.
     */
    @Test
    public void testRandomRelatedPortletsFailsGracefully() {

        when(marketplaceService.marketplacePortletDefinitionsBrowseableBy(any(IPerson.class)))
            .thenThrow(RuntimeException.class);

        assertTrue(marketplacePortletDefinition.getRandomSamplingRelatedPortlets().isEmpty());
    }

    /**
     * Test that random related portlets filters to those portlets browseable by the current user.
     */
    @Test
    public void testRandomRelatedPortletsFiltersToBrowseable() {

        final Set<MarketplacePortletDefinition> allBrowseablePortlets = new HashSet<>();
        allBrowseablePortlets.add(this.browseableRelatedDefinition1);
        allBrowseablePortlets.add(this.browseableRelatedDefinition2);
        allBrowseablePortlets.add(this.browseableDefinition1);
        allBrowseablePortlets.add(this.browseableDefinition2);

        when(marketplaceService.marketplacePortletDefinitionsBrowseableBy(person))
            .thenReturn(ImmutableSet.copyOf(allBrowseablePortlets));

        final Set<MarketplacePortletDefinition> expectedBrowseableRelated = new HashSet<>();
        expectedBrowseableRelated.add(this.browseableRelatedDefinition1);
        expectedBrowseableRelated.add(this.browseableRelatedDefinition2);

        assertEquals(expectedBrowseableRelated,
            marketplacePortletDefinition.getRandomSamplingRelatedPortlets());

    }

    /**
     * Test implementation of equals() for MarketplacePortletDefinitions.
     */
    @Test
    public void testEquals() {

        marketplacePortletDefinition =
            new MarketplacePortletDefinition(portletDefinition, person /* with a person! */,
                marketplaceService, categoryRegistry);

        assertFalse(marketplacePortletDefinition.equals(null));

        assertFalse(marketplacePortletDefinition.equals("SomeStringObject"));

        final MarketplacePortletDefinition anotherMarketplacePortletDefinition =
            new MarketplacePortletDefinition(anotherPortletDefinition, person, /* with a person! */
                marketplaceService, categoryRegistry);

        assertTrue(marketplacePortletDefinition.equals(anotherMarketplacePortletDefinition));

        when(portletDefinition.getFName()).thenReturn("snooper");

        assertFalse(marketplacePortletDefinition.equals(anotherMarketplacePortletDefinition));

        when(anotherPortletDefinition.getFName()).thenReturn("snooper");

        assertTrue(marketplacePortletDefinition.equals(anotherMarketplacePortletDefinition));
        assertEquals(marketplacePortletDefinition.hashCode(),
            anotherMarketplacePortletDefinition.hashCode());

        final MarketplacePortletDefinition definitionWithDifferentPerson =
            new MarketplacePortletDefinition(portletDefinition, anotherPerson,
                marketplaceService, categoryRegistry);

        assertFalse(marketplacePortletDefinition.equals(definitionWithDifferentPerson));

        when(anotherPortletDefinition.getFName()).thenReturn("marketplace");

        assertFalse(marketplacePortletDefinition.equals(anotherMarketplacePortletDefinition));
    }

}
