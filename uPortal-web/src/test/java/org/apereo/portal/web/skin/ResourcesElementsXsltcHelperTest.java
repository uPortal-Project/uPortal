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
package org.apereo.portal.web.skin;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.DocumentBuilderFactory;
import org.jasig.resourceserver.utils.aggr.ResourcesElementsProvider;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * The skin's head elements, which every page render copies into the page.
 *
 * <p>With aggregation on, {@code ResourcesElementsProviderImpl} caches one {@code DocumentFragment}
 * per skin and returns it to every request, as the provider here does. Reading one DOM node list
 * from several threads corrupts the cursor the document keeps for it, and {@code item(int)} returns
 * {@code null} or the wrong element. A page render then failed in {@code skinResources} with a
 * {@code NullPointerException}.
 */
public class ResourcesElementsXsltcHelperTest {
    private static final String SKIN = "/media/skins/respondr/defaultSkin/skin.xml";
    private static final int ELEMENTS = 12;
    private static final int THREADS = 4;
    private static final long READ_FOR_MS = 3000;

    private final HttpServletRequest request = new MockHttpServletRequest();
    private NodeList shared;
    private ResourcesElementsXsltcHelper helper;

    @Before
    public void setUp() throws Exception {
        final Document document =
                DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        final DocumentFragment fragment = document.createDocumentFragment();
        for (int i = 0; i < ELEMENTS; i++) {
            final Element element = document.createElement(i % 2 == 0 ? "link" : "script");
            element.setAttribute("href", href(i));
            fragment.appendChild(element);
        }
        shared = fragment.getChildNodes();

        final ResourcesElementsProvider provider =
                mock(ResourcesElementsProvider.class, withSettings().stubOnly());
        when(provider.getResourcesXmlFragment(any(), eq(SKIN))).thenReturn(shared);
        helper = new ResourcesElementsXsltcHelper();
        helper.setResourcesDao(provider);
    }

    @Test
    public void testEachCallGetsItsOwnCopyOfTheSkinElements() {
        final NodeList first = helper.getResourcesXmlFragment(request, SKIN);
        final NodeList second = helper.getResourcesXmlFragment(request, SKIN);

        assertEquals(ELEMENTS, first.getLength());
        assertEquals(ELEMENTS, second.getLength());
        for (int i = 0; i < ELEMENTS; i++) {
            assertTrue("element " + i, shared.item(i).isEqualNode(first.item(i)));
            assertTrue("element " + i, shared.item(i).isEqualNode(second.item(i)));
            assertNotSame("element " + i, shared.item(i), first.item(i));
            assertNotSame("element " + i, first.item(i), second.item(i));
        }
    }

    /** Several renders read the elements at once, as XSLTC's {@code nodeList2Iterator} does. */
    @Test
    public void testRendersAtTheSameTimeReadEveryElement() throws Exception {
        final ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        try {
            final CountDownLatch start = new CountDownLatch(1);
            final long end = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(READ_FOR_MS);
            final List<Future<String>> results = new ArrayList<>();
            for (int t = 0; t < THREADS; t++) {
                results.add(executor.submit(readUntil(start, end)));
            }
            start.countDown();
            for (Future<String> result : results) {
                assertEquals("", result.get(READ_FOR_MS * 10, TimeUnit.MILLISECONDS));
            }
        } finally {
            executor.shutdownNow();
        }
    }

    /** Reads every element until the end, and returns the first wrong read ("" if none). */
    private Callable<String> readUntil(CountDownLatch start, long end) {
        return () -> {
            start.await();
            for (int round = 0; System.nanoTime() < end; round++) {
                final NodeList elements = helper.getResourcesXmlFragment(request, SKIN);
                final int length = elements.getLength();
                if (length != ELEMENTS) {
                    return "round " + round + ": " + length + " elements";
                }
                for (int i = 0; i < length; i++) {
                    final Node element = elements.item(i);
                    if (element == null) {
                        return "round " + round + ": element " + i + " is null";
                    }
                    if (element.getNodeType() != Node.ELEMENT_NODE
                            || !href(i).equals(((Element) element).getAttribute("href"))) {
                        return "round " + round + ": element " + i + " is " + element;
                    }
                }
            }
            return "";
        };
    }

    private static String href(int i) {
        return "/ResourceServingWebapp/rs/skin-" + i + ".css";
    }
}
