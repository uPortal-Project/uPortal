/**
 * 
 */
package org.jasig.portal;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.spring.locator.PortalRequestUtilsLocator;
import org.jasig.portal.spring.locator.PortalUrlProviderLocator;
import org.jasig.portal.url.IChannelPortalUrl;
import org.jasig.portal.url.IPortalRequestInfo;
import org.jasig.portal.url.IPortalRequestUtils;
import org.jasig.portal.url.IPortalUrlProvider;

/**
 * This class is an attempt to refactor the behavior found in {@link UPFileSpec}.
 * 
 * @author Nicholas Blair, nblair@doit.wisc.edu
 *
 */
public class UPFileSpecRefactor {

	/**
	 * Used to designate user layout root node in .uP files
	 */
	public static final String USER_LAYOUT_ROOT_NODE = "userLayoutRootNode";
	// some URL construction elements
	public static final String TARGET_URL_ELEMENT = "target";
	public static final String WORKER_URL_ELEMENT = "worker";
	public static final String DETACH_URL_ELEMENT = "detach";
	public static final String RENDER_URL_ELEMENT = "render";
	public static final String PORTAL_URL_SEPARATOR = ".";
	public static final String PORTAL_URL_SUFFIX = "uP";
	// individual worker URL elements
	public static final String FILE_DOWNLOAD_WORKER = "download";
	// int values for methods
	public static final int RENDER_METHOD = 0;
	public static final int WORKER_METHOD = 1;
	
	private IPortalRequestInfo portalRequestInfo;
	private IChannelPortalUrl channelPortalUrl;
	
	/**
	 * 
	 */
	public UPFileSpecRefactor() {
		this(PortalRequestUtilsLocator.getPortalRequestUtils().getCurrentPortalRequest());
	}
	
	/**
	 * 
	 * @param request
	 */
	public UPFileSpecRefactor(HttpServletRequest request) {
		IPortalUrlProvider portalUrlProvider = PortalUrlProviderLocator.getPortalUrlProvider();
		this.portalRequestInfo = portalUrlProvider.getPortalRequestInfo(request);
		
		if(portalRequestInfo.getTargetedChannelSubscribeId() != null) {
			channelPortalUrl = portalUrlProvider.getChannelUrlByNodeId(request, portalRequestInfo.getTargetedChannelSubscribeId());
		}
	}
	
	/**
	 * DO NOT implement - original UPFileSpec has a constructor with a single String arg, but only used by test cases.
	 * @param url
	 */
	@Deprecated
	public UPFileSpecRefactor(String url) {
	}
	
	/**
	 * Copy constructor.
	 * 
	 * @param toCopy
	 */
	public UPFileSpecRefactor(UPFileSpecRefactor toCopy) {
		this.portalRequestInfo = toCopy.portalRequestInfo;
		this.channelPortalUrl = toCopy.channelPortalUrl;
	}
	
	/**
	 * 
	 * @param method
	 * @param methodNodeId
	 * @param targetNodeId
	 * @param extraElements
	 * @throws PortalException
	 */
	public UPFileSpecRefactor(int method,String methodNodeId,String targetNodeId,String extraElements) throws PortalException {
		if(method != RENDER_METHOD && method != WORKER_METHOD) {
			throw new PortalException("Unacceptable value for method (integer) argument: " + method);
		}
		
		// method node Id can be ignored?
		
		
		// extraElements can be ignored
	}
	
	/**
	 * Determine method name
	 *
	 * @return a <code>String</code> method name,  <code>null</code> if no method was specified.
	 */
	public String getMethod() {
		if(null != this.channelPortalUrl) {
			if(this.channelPortalUrl.isWorker()) {
				return WORKER_URL_ELEMENT; 
			} else {
				return RENDER_URL_ELEMENT;
			}
		} else {
			return null;
		}
	}

	/**
	 * Determine Id specified by the method element.
	 *
	 * @return a <code>String</code> method node Id value, <code>null</code> if no method was specified.
	 */
	public String getMethodNodeId() {
		return portalRequestInfo.getTargetedLayoutNodeId();
	}

	/**
	 * Determine Id specified by the "target" element.
	 *
	 * @return a <code>String</code> target Id value, <code>null</code> if no target was specified.
	 */
	public String getTargetNodeId() {
		return portalRequestInfo.getTargetedChannelSubscribeId();
	}

	/**
	 * Get the full .uP file <code>String</code>.
	 *
	 * @return a <code>String</code> value
	 */
	public String getUPFile() throws PortalException {
		return this.channelPortalUrl.getUrlString();
	}

	/**
	 * Returns a "cleaned-up" version of the uP file with all known
	 * fields such as tag, method, and target, removed. This can be used by...
	 *
	 * NOTE: Always returns an empty string.
	 * 
	 * @return a <code>String</code> value, <code>null</code> if none were encountered.
	 */
	public String getUPFileExtras() {
		return "";
	}

	/**
	 * Set a method.
	 *
	 * @param method a method <code>String</code> value (required, must be one of the <code>UPFileSpec.*_METHOD</code> constants, i.e.  {@link #RENDER_METHOD} or {@link #WORKER_METHOD})
	 * @exception PortalException if an invalid method id is passed.
	 */
	public void setMethod(int method) throws PortalException {
		// IGNORE THIS CALL
		// only callers in original implementation were in constructor and in test cases
	}

	/**
	 * Set method node id.
	 *
	 * @param nodeId a <code>String</code> value
	 */
	public void setMethodNodeId(String nodeId) {
		// IGNORE THIS CALL
		// methodNodeId's equivalent in our new model is targetedLayoutNodeId
		//portalRequestInfo.getTargetedLayoutNodeId();
	}

	/**
	 * Set target node id
	 *
	 * @param nodeId a <code>String</code> value
	 */
	public void setTargetNodeId(String nodeId) {
		// IGNORE THIS CALL?
		// targetNodeId's equivalent is targetedChannelSubscribeId
		//portalRequestInfo.getTargetedChannelSubscribeId();
	}

	/**
	 * Set extras to be appended to the spec before the suffix element (".uP")
	 *
	 * @param extras a <code>String</code> value
	 */
	public void setUPFileExtras(String extras) {
		// IGNORE THIS CALL 
		// shouldn't have been public in the first place, only callers in original implementation were internal
	}

}
