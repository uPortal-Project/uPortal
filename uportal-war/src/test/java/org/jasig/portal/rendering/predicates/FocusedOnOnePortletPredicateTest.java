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
package org.jasig.portal.rendering.predicates;

import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IUrlSyntaxProvider;
import org.jasig.portal.url.UrlState;
import org.jasig.portal.url.UrlType;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import javax.servlet.http.HttpServletRequest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;

/**
 * Unit tests for FocusedOnOnePortletPredicate.
 * @since uPortal 4.2
 */
public class FocusedOnOnePortletPredicateTest {

    @Mock private HttpServletRequest mockRequest;

    @Mock private IUrlSyntaxProvider syntaxProvider;

    @Mock private IPortalRequestInfo portalRequestInfo;

    private FocusedOnOnePortletPredicate predicate;

    @Before
    public void beforeTests() {

        initMocks(this);

        this.predicate = new FocusedOnOnePortletPredicate();
        this.predicate.setUrlSyntaxProvider(this.syntaxProvider);
    }

    /**
     * Test that when the URL is a NORMAL url state URL, and thus one addressing a mosaic of normal-mode portlets,
     * returns false.
     */
    @Test
    public void falseWhenAddressesDashboard() {

        when(this.syntaxProvider.getPortalRequestInfo(mockRequest)).thenReturn(this.portalRequestInfo);
        when(this.portalRequestInfo.getUrlState()).thenReturn(UrlState.NORMAL);

        predicate.setUrlSyntaxProvider(this.syntaxProvider);

        assertFalse(predicate.apply(this.mockRequest));

    }

    /**
     * Test that When the request addresses a maximized (and thus, focused-upon) portlet, returns true.
     */
    @Test
    public void trueWhenAddressesSpecificPortletMaximized() {

        when(this.syntaxProvider.getPortalRequestInfo(mockRequest)).thenReturn(this.portalRequestInfo);
        when(this.portalRequestInfo.getUrlState()).thenReturn(UrlState.MAX);

        assertTrue(predicate.apply(this.mockRequest));

    }

    /**
     * Test that When the request addresses an exclusive (and thus, focused-upon) portlet, returns true.
     */
    @Test
    public void trueWhenAddressesSpecificPortletExclusive() {

        when(this.syntaxProvider.getPortalRequestInfo(mockRequest)).thenReturn(this.portalRequestInfo);
        when(this.portalRequestInfo.getUrlState()).thenReturn(UrlState.EXCLUSIVE);

        assertTrue(predicate.apply(this.mockRequest));

    }

    /**
     * Test that When the request addresses a detached (and thus, focused-upon) portlet, returns true.
     */
    @Test
    public void trueWhenAddressesSpecificPortletDetached() {

        when(this.syntaxProvider.getPortalRequestInfo(mockRequest)).thenReturn(this.portalRequestInfo);
        when(this.portalRequestInfo.getUrlState()).thenReturn(UrlState.DETACHED);

        assertTrue(predicate.apply(this.mockRequest));

    }

    /**
     * If for whatever reason the portal request info returned form the syntax provider is null,
     * return false, since null does not represent a request focused on a single portlet.
     */
    @Test
    public void falseWhenPortalRequestInfoNull() {

        when(this.syntaxProvider.getPortalRequestInfo(mockRequest)).thenReturn(null);

        assertFalse(predicate.apply(this.mockRequest));

    }

    @Test
    public void hasFriendlyToString() {

        assertEquals("FocusedOnOnePortletPredicate", predicate.toString());

    }


}
