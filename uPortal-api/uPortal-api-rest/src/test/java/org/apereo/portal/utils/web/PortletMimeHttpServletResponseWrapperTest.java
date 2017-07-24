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
package org.apereo.portal.utils.web;

import java.io.IOException;
import javax.servlet.ServletOutputStream;
import org.apereo.portal.portlet.container.cache.CacheControlImpl;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class PortletMimeHttpServletResponseWrapperTest {

    private PortletMimeHttpServletResponseWrapper portletMimeHttpServletResponseWrapper;
    private MockHttpServletRequest req;
    private MockHttpServletResponse res;

    @Before
    public void setup() {
        res = new MockHttpServletResponse();
        req = new MockHttpServletRequest();
    }

    @Test
    public void testGetOutputStream() throws IOException {
        portletMimeHttpServletResponseWrapper =
                new PortletMimeHttpServletResponseWrapper(res, null, null, new CacheControlImpl());
        ServletOutputStream stream = portletMimeHttpServletResponseWrapper.getOutputStream();
        Assert.assertNotNull(stream);
    }
}
