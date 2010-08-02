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

package org.jasig.portal.portlets.image;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;

import static org.junit.Assert.assertEquals;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;

import org.junit.Test;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ImagePortletControllerTest {
    @Test
    public void testSpelPreferences() {
        final ImagePortletController controller = new ImagePortletController();
        
        final PortletPreferences preferences = createMock(PortletPreferences.class);
        expect(preferences.getValue("img-uri-spel", null)).andReturn("contextPath + '/media/org/jasig/portal/channels/CImage/admin_feature.png'");
        
        final RenderRequest request = createMock(RenderRequest.class);
        expect(request.getPreferences()).andReturn(preferences);
        expect(request.getContextPath()).andReturn("/uPortal");
        
        replay(request, preferences);
        
        final String url = controller.getPreference("img-uri", request);
        assertEquals("/uPortal/media/org/jasig/portal/channels/CImage/admin_feature.png", url);
        
        verify(request, preferences);
    }
}
