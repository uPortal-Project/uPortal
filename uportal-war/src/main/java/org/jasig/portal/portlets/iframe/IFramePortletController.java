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

package org.jasig.portal.portlets.iframe;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.ModelAndView;
import org.springframework.web.portlet.mvc.AbstractController;

/**
 * This portlet renders content identified by a URL within an inline browser
 * frame. See
 * <a href="http://www.htmlhelp.com/reference/html40/special/iframe.html">
 * http://www.htmlhelp.com/reference/html40/special/iframe.html</a> for more
 * information on inline frames.
 *
 * @author Susan Bramhall
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 */
public class IFramePortletController extends AbstractController {
    
    protected static final Map<String, String> IFRAME_ATTRS = Collections.unmodifiableMap(new LinkedHashMap<String, String>() {{
        /** document-wide unique id */
        put("id", null);
        
        /** space-separated list of classes */
        put("cssClass", null);
        
        /** associated style info */
        put("style", null);
        
        /** advisory title */
        put("title", null);
        
        /** link to long description (complements title) */
        put("longDescription", null);
        
        /** name of frame for targetting */
        put("name", null);
        
        /** source of frame content */
        put("src", null);
        
        /** request frame borders? */
        put("frameBorder", "0");
        
        /** margin widths in pixels */
        put("marginWidth", null);
        
        /** margin height in pixels */
        put("marginHeight", null);
        
        /** scrollbar or none */
        put("scrolling", null);
        
        /** vertical or horizontal alignment */
        put("align", null);
        
        /** frame height */
        put("width", "100%");
        
        /** frame width */
        put("height", null);
    }});


	@Override
	protected ModelAndView handleRenderRequestInternal(RenderRequest request,
			RenderResponse response) throws Exception {
		
		Map<String,Object> model = new HashMap<String,Object>();
		
		// get the IFrame target URL and the configured height of the IFrame
		// window from the portlet preferences
		PortletPreferences preferences = request.getPreferences();
		
		for (final Map.Entry<String, String> attrEntry : IFRAME_ATTRS.entrySet()) {
    		final String attr = attrEntry.getKey();
            final String defaultValue = attrEntry.getValue();
            model.put(attr, preferences.getValue(attr, defaultValue));
		}
		
        //Legacy support for url attribute
		if (model.get("src") == null) {
	        model.put("src", preferences.getValue("url", IFRAME_ATTRS.get("src")));	        
		}
		
		return new ModelAndView("/jsp/IFrame/iframePortlet", "attrs", model);
	}

}
