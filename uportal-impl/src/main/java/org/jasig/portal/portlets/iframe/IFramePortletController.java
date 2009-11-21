package org.jasig.portal.portlets.iframe;

import java.util.HashMap;
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

	@Override
	protected ModelAndView handleRenderRequestInternal(RenderRequest request,
			RenderResponse response) throws Exception {
		
		Map<String,Object> model = new HashMap<String,Object>();
		
		// get the IFrame target URL and the configured height of the IFrame
		// window from the portlet preferences
		PortletPreferences preferences = request.getPreferences();
		model.put("url", preferences.getValue("url", null));
		model.put("height", preferences.getValue("height", null));
		
		return new ModelAndView("/jsp/IFrame/iframePortlet", model);
	}

}
