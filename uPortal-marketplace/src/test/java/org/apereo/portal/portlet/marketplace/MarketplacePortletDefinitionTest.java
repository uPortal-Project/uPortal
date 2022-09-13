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
package org.apereo.portal.portlet.marketplace;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

import org.apereo.portal.portlet.om.IPortletDefinition;
import org.apereo.portal.portlet.registry.IPortletCategoryRegistry;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

/** Unit tests for MarketplacePortletDefinition. */
public class MarketplacePortletDefinitionTest {

    @Mock IPortletDefinition portletDefinition;
    @Mock IPortletDefinition anotherPortletDefinition;

    @Mock IPortletCategoryRegistry categoryRegistry;

    @Mock IMarketplaceService service;

    MarketplacePortletDefinition marketplacePortletDefinition;

    @Before
    public void setUp() {
        initMocks(this);

        marketplacePortletDefinition =
                new MarketplacePortletDefinition(portletDefinition, service, categoryRegistry);
    }

    /**
     * Test that a MarketplacePortletDefinition passes through the alternativeMaximizedLink of the
     * IPortletDefinition it wraps.
     */
    @Test
    public void testReflectsWrappedDefinitionAlternativeMaximizedLink() {

        when(portletDefinition.getAlternativeMaximizedLink()).thenReturn("http://apereo.org");

        assertEquals(
                "http://apereo.org", marketplacePortletDefinition.getAlternativeMaximizedLink());
    }

    /** Test that when there is an alternative maximized link, the render URL is that link. */
    @Test
    public void testRenderUrlIsAlternativeMaximizedLinkWhenPresent() {

        when(portletDefinition.getAlternativeMaximizedLink()).thenReturn("http://apereo.org");

        assertEquals("http://apereo.org", marketplacePortletDefinition.getRenderUrl());
    }

    /**
     * Test that a MarketplacePortletDefinition passes through the alternativeMaximizedLinkTarget of
     * the IPortletDefinition it wraps.
     */
    @Test
    public void testReflectsWrappedDefinitionAlternativeMaximizedLinkTarget() {

        when(portletDefinition.getAlternativeMaximizedLinkTarget()).thenReturn("_self");

        assertEquals("_self", marketplacePortletDefinition.getAlternativeMaximizedLinkTarget());
    }

    /** Test implementation of equals(). */
    @Test
    public void testEquals() {

        assertFalse(marketplacePortletDefinition.equals(null));

        assertFalse(marketplacePortletDefinition.equals("SomeStringObject"));

        final MarketplacePortletDefinition anotherMarketplacePortletDefinition =
                new MarketplacePortletDefinition(
                        anotherPortletDefinition, service, categoryRegistry);

        // fnames of both are null, so they are equal
        assertTrue(marketplacePortletDefinition.equals(anotherMarketplacePortletDefinition));

        when(portletDefinition.getFName()).thenReturn("snooper");

        assertFalse(marketplacePortletDefinition.equals(anotherMarketplacePortletDefinition));

        when(anotherPortletDefinition.getFName()).thenReturn("snooper");

        assertTrue(marketplacePortletDefinition.equals(anotherMarketplacePortletDefinition));
        assertEquals(
                marketplacePortletDefinition.hashCode(),
                anotherMarketplacePortletDefinition.hashCode());

        when(anotherPortletDefinition.getFName()).thenReturn("marketplace");

        assertFalse(marketplacePortletDefinition.equals(anotherMarketplacePortletDefinition));
    }
}
