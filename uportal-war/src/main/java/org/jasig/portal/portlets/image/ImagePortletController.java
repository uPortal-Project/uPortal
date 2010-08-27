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

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.jasig.portal.spring.spel.IPortalSpELService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.context.PortletWebRequest;
import org.springframework.web.portlet.mvc.AbstractController;

/** <p>A simple portlet which renders an image along with an optional
 * caption and subcaption.</p>
 * <p>Portlet preferences:</p>
 *   <table>
 *     <tr><th>Name</th><th>Description</th><th>Example</th><th>Required</th></tr>
 *     <tr><td>img-uri</td><td>The URI of the image to display</td><td>http://webcam.its.hawaii.edu/uhmwebcam/image01.jpg</td><td>yes</td></tr>
 *     <tr><td>img-width</td><td>The width of the image to display</td><td>320</td><td>no</td></tr>
 *     <tr><td>img-height</td><td>The height of the image to display</td><td>240</td><td>no</td></tr>
 *     <tr><td>img-border</td><td>The border of the image to display</td><td>0</td><td>no</td></tr>
 *     <tr><td>img-link</td><td>A URI to be used as an href for the image</td><td>http://www.hawaii.edu/visitor/#webcams</td><td>no</td></tr>
 *     <tr><td>caption</td><td>A caption of the image to display</td><td>Almost Live Shot of Hamilton Library Front Entrance</td><td>no</td></tr>
 *     <tr><td>subcaption</td><td>The subcaption of the image to display</td><td>Updated Once per Minute During Daylight Hours</td><td>no</td></tr>
 *     <tr><td>alt-text</td><td>Text to include as the 'alt' attribute of the img tag</td><td>Almost live shot of Hamilton library front enterance</td><td>no, but highly recommended in support of non-visual browsers</td></tr>
 *   </table>
 * @author Ken Weiner, kweiner@unicon.net
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class ImagePortletController extends AbstractController {
    private IPortalSpELService portalSpELService;
    
    @Autowired
    public void setPortalSpELService(IPortalSpELService portalSpELService) {
        this.portalSpELService = portalSpELService;
    }

    @Override
	protected ModelAndView handleRenderRequestInternal(RenderRequest request,
			RenderResponse response) throws Exception {
		
		final Map<String,Object> model = new HashMap<String,Object>();
		
		final PortletPreferences preferences = request.getPreferences();
        final PortletWebRequest webRequest = new PortletWebRequest(request);
		
		// retrieve configuration information about the image from the portlet
		// preferences
		model.put("uri", getPreference("img-uri", webRequest, preferences));
		model.put("width", getPreference("img-width", webRequest, preferences));
		model.put("height", getPreference("img-height", webRequest, preferences));
		model.put("border", getPreference("img-border", webRequest, preferences));
		model.put("link", getPreference("img-link", webRequest, preferences));
		model.put("caption", getPreference("caption", webRequest, preferences));
		model.put("subcaption", getPreference("subcaption", webRequest, preferences));
		model.put("alt", getPreference("alt-text", webRequest, preferences));
		
		return new ModelAndView("/jsp/Image/imagePortlet", model);
	}

    protected String getPreference(String name, WebRequest request, PortletPreferences preferences) {
        final String value = preferences.getValue(name, null);
        if (value != null) {
            return this.portalSpELService.parseString(value, request);
        }
        
        return value;
    }
}
