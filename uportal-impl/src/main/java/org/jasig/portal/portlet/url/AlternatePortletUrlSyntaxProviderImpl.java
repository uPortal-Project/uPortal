/**
 * 
 */
package org.jasig.portal.portlet.url;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.Validate;
import org.jasig.portal.portlet.om.IPortletWindow;
import org.jasig.portal.portlet.om.IPortletWindowId;
import org.jasig.portal.url.IPortalPortletUrl;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortalUrlProvider;
import org.jasig.portal.utils.Tuple;

/**
 * New implementation of {@link IPortletUrlSyntaxProvider} for
 * UP-2045.
 * 
 * Requires a {@link IPortalUrlProvider} be set.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 *
 */
public class AlternatePortletUrlSyntaxProviderImpl implements
		IPortletUrlSyntaxProvider {

	private IPortalUrlProvider portalUrlProvider;
	
	/**
	 * @param portalUrlProvider the portalUrlProvider to set
	 */
	public void setPortalUrlProvider(final IPortalUrlProvider portalUrlProvider) {
		this.portalUrlProvider = portalUrlProvider;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider#generatePortletUrl(javax.servlet.http.HttpServletRequest, org.jasig.portal.portlet.om.IPortletWindow, org.jasig.portal.portlet.url.PortletUrl)
	 */
	public String generatePortletUrl(HttpServletRequest request,
			IPortletWindow portletWindow, PortletUrl portletUrl) {
		IPortalPortletUrl portalPortletUrl = portalUrlProvider.getPortletUrl(request, portletWindow.getPortletWindowId());
		portalPortletUrl = mergeWithPortletUrl(portalPortletUrl, portletUrl);
		return portalPortletUrl.toString();
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.portlet.url.IPortletUrlSyntaxProvider#parsePortletParameters(javax.servlet.http.HttpServletRequest)
	 */
	public Tuple<IPortletWindowId, PortletUrl> parsePortletParameters(
			HttpServletRequest request) {
		IPortalRequestInfo requestInfo = portalUrlProvider.getPortalRequestInfo(request);
		if(null == requestInfo.getTargetedPortletWindowId()) {
			return null;
		} else {
			IPortletWindowId portletWindowId = requestInfo.getTargetedPortletWindowId(); 
			IPortalPortletUrl portalPortletUrl = portalUrlProvider.getPortletUrl(request, portletWindowId);
			Tuple<IPortletWindowId, PortletUrl> result = new Tuple<IPortletWindowId, PortletUrl>(portletWindowId, toPortletUrl(portalPortletUrl));
			return result;
		}
	}
	
	/**
	 * Convert a {@link IPortalPortletUrl} into a {@link PortletUrl}.
	 * 
	 * @param portalPortletUrl
	 * @return
	 */
	protected static PortletUrl toPortletUrl(final IPortalPortletUrl portalPortletUrl) {
		PortletUrl result = new PortletUrl();
		Map<String, String[]> parameters = new HashMap<String, String[]>();
		Map<String, List<String>> argParameters = portalPortletUrl.getPortletParameters();
		for(String key: argParameters.keySet()) {
			String [] asArray = argParameters.get(key).toArray(new String[]{ });
			parameters.put(key, asArray);
		}
		result.setParameters(parameters);
		
		result.setPortletMode(portalPortletUrl.getPortletMode());
		
		if(portalPortletUrl.isAction()) {
			result.setRequestType(RequestType.ACTION);
		} else {
			result.setRequestType(RequestType.RENDER);
		}
		
		// null is the default value for the secure field
		//result.setSecure(null);
		
		result.setWindowState(portalPortletUrl.getWindowState());
		return result;
	}
	
	/**
	 * The purpose of this method is to port the fields of the {@link PortletUrl} argument
	 * to the appropriate fields of the {@link IPortalPortletUrl} argument.
	 * 
	 * This method mutates the {@link IPortalPortletUrl} argument and return it.
	 * 
	 * Neither argument can be null.
	 * 
	 * @param original
	 * @param mergeWith
	 * @return the updated original {@link IPortalPortletUrl}
	 */
	protected static IPortalPortletUrl mergeWithPortletUrl(IPortalPortletUrl original, PortletUrl mergeWith) {
		Validate.notNull(original, "original IPortalPortletUrl must not be null");
		Validate.notNull(mergeWith, "mergeWith PortletUrl must not be null");
		if(RequestType.ACTION.equals(mergeWith.getRequestType())) {
			original.setAction(true);
		}
		original.setPortletMode(mergeWith.getPortletMode());
		
		original.setWindowState(mergeWith.getWindowState());
		
		Map<String, String[]> mergeWithParameters = mergeWith.getParameters();
		for(String key: mergeWithParameters.keySet()) {
			original.setPortalParameter(key, mergeWithParameters.get(key));
		}
		return original;
	}

}
