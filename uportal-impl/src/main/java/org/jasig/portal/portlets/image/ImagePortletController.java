package org.jasig.portal.portlets.image;

import java.util.HashMap;
import java.util.Map;

import javax.portlet.PortletPreferences;
import javax.portlet.RenderRequest;
import javax.portlet.RenderResponse;

import org.springframework.web.portlet.ModelAndView;
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

	@Override
	protected ModelAndView handleRenderRequestInternal(RenderRequest request,
			RenderResponse response) throws Exception {
		Map<String,Object> model = new HashMap<String,Object>();
		
		// retrieve configuration information about the image from the portlet
		// preferences
		PortletPreferences preferences = request.getPreferences();
		model.put("uri", preferences.getValue("img-uri", null));
		model.put("width", preferences.getValue("img-width", null));
		model.put("height", preferences.getValue("img-height", null));
		model.put("border", preferences.getValue("img-border", null));
		model.put("link", preferences.getValue("img-link", null));
		model.put("caption", preferences.getValue("caption", null));
		model.put("subcaption", preferences.getValue("subcaption", null));
		model.put("alt", preferences.getValue("alt-text", null));
		
		return new ModelAndView("/jsp/Image/imagePortlet", model);
	}

	
}
