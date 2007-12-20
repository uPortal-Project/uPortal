/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.UPFileSpec;

/**
 * The URLUtil class offers static helper methods for manipulating the
 * request parameters and URLs of both HTTP GET and HTTP POST requests
 * and performing redirections based on the new parameters and URLs. 
 * and request parameters.
 * 
 * @author Andreas Christoforides, achristoforides@unicon.net
 * @author Nick Bolton, nbolton@unicon.net
 * @version $Revision$
 */
 
public class URLUtil
{
    
    private static final Log log = LogFactory.getLog(URLUtil.class);
    
    public static final int REDIRECT_URL_LIMIT =  512;    
    
    public static final String HTTP_GET_REQUEST  = "GET";
    public static final String HTTP_POST_REQUEST = "POST";

    /**
     * Performs a redirect for both HTTP GET and HTTP POST requests
     * based on the the specified target channel and parameters to be ignored.
     *
     * @param req An HttpServletRequest object.     
     * @param res An HttpServletResponse object.
     * @param targetNodeId The target node Id of a channel.
     * @param ignoreParams An array of String objects containing
     * the parameters to be ignored.
     */
    public static void redirect(HttpServletRequest req, HttpServletResponse res,
        String targetNodeId, boolean asRoot, String[] ignoreParams,
        String charset)
    throws PortalException {
        String extras = new UPFileSpec(req).getUPFileExtras();
        UPFileSpec uPFileSpec = buildUPFileSpec(targetNodeId, extras, asRoot); 

        // only handles get methods at this time
        redirectGet(req, res, uPFileSpec, ignoreParams);
    
        // Determine if this is an HTTP GET or POST request
/*
        if (httpMethod.equalsIgnoreCase(HTTP_GET_REQUEST)) {
            redirectGet(req, res, uPFileSpec, ignoreParams);
        } else if (httpMethod.equalsIgnoreCase(HTTP_POST_REQUEST)) {
            redirectPost(req, res, uPFileSpec, ignoreParams, charset);
        }
*/
    }      
   
    /**
     * Uses an HttpServletRequest object and a String array that
     * contains the names of parameters to be ignored to construct
     * a filtered name-value pair string.
     *
     * @param req        A HttpServletRequest object.
     * @param ignoreParams An array of String objects representing
     * the parameters to be ignored.
     *
     * @return A String of name-value pairs representing a query string. .
     */
    private static String buildRequestParams (HttpServletRequest req,
        String[] ignoreParams) {
        String   parameterName   = null;
        String[] parameterValues = null;
    
        StringBuffer sb = new StringBuffer(REDIRECT_URL_LIMIT);
        
        // Get all parameters 
        Enumeration requestParameterNames = req.getParameterNames();
        
        // Walk through all parameter names
        while (requestParameterNames.hasMoreElements()) {
            // Get parameter name
            parameterName = (String) requestParameterNames.nextElement();
        
            // Exclude the parameter that are in the ignoreParams array
            // and only include parameters If they came from 
            if (!parameterExistsIn(parameterName, ignoreParams)) {
                // Get all values of parameter in case it is a
                // multi-value parameter
                parameterValues = req.getParameterValues(parameterName);
            
                // Add parameter name-value pairs
                for (int index = 0; index < parameterValues.length; index++) {
                    sb.append(parameterName);
                    sb.append("=");
                    sb.append(parameterValues[index]);
                    sb.append("&");
                }
            }
        }
        
        // Truncate the  extra '&' from the end if one exists
        if (sb.length() != 0 && sb.charAt(sb.length() - 1) == '&') {
            sb.setLength(sb.length() - 1);
        }
    
        return sb.toString();
    }   
    
    /**
     * Determines if the specified parameter exists within
     * the specified String array.
     * 
     * @param param The parameter name to search for in the array of parameters.
     * @param params The array of parameters to search in.
     * 
     * @return Returns true if the parameter name is found, false otherwise.
     */
    private static boolean parameterExistsIn (String param, String[] params) {
        boolean found = false;
        
        for (int index = 0; index < params.length; index++) {
            if (param.equals(params[index])) {
                found = true;    
            }
        }
        
        return found;
    }
    
    
    /**
     * Constructs a generic UPFileSpec object using the specified
     * target node id.
     * 
     * @param targetNodeId The target node id to be used in the
     * UPFileSpec object.
     * 
     * @return A generic UPFileSpec object. 
     */
    private static UPFileSpec buildUPFileSpec (String targetNodeId,
        String extras, boolean asRoot)
    throws PortalException {
        UPFileSpec up = null;

        if (asRoot) {
            up = new UPFileSpec(PortalSessionManager.IDEMPOTENT_URL_TAG,
                UPFileSpec.RENDER_METHOD, targetNodeId, null, extras);
        } else {
            up = new UPFileSpec(PortalSessionManager.IDEMPOTENT_URL_TAG,
                UPFileSpec.RENDER_METHOD, UPFileSpec.USER_LAYOUT_ROOT_NODE,
                targetNodeId, extras);
        }
    
        return up;
    }
    
    /**
     * Performs a HTTP GET redirect using the specified UPFileSpec
     * and parameters to be ignored.
     * 
     * @param req An HttpServletRequest object.     
     * @param res An HttpServletResponse object.
     * @param up the uPortal file spec.
     * @param ignoreParams An array of String objects containing
     * the parameters to be ignored.
     */
     public static void redirectGet(HttpServletRequest req,
         HttpServletResponse res, UPFileSpec up, String[] ignoreParams)
     throws PortalException {
        
        StringBuffer sb = new StringBuffer(REDIRECT_URL_LIMIT);
        
        try {
            sb.append(up.getUPFile());
            String qs = req.getQueryString();
            if (qs != null && !"".equals(qs)) {
                sb.append('?').append(buildRequestParams(req, ignoreParams));
            }
            if (log.isDebugEnabled())
                log.debug(
                        "URLUtil::redirectGet() " +
                        "Redirecting to framework: " + sb.toString());
             res.sendRedirect(res.encodeRedirectURL(sb.toString()));
        } catch (IOException ioe) {
            log.error(
                "URLUtil::redirectGet() " +
                "Failed redirecting to framework: " + sb.toString(), ioe);
            throw new PortalException(ioe);
        }
    }

    private static void buildHeader(HttpServletRequest req,
        HttpURLConnection uconn) {
        String name;
        String value;
        String currentValue;
        Enumeration fields = req.getHeaderNames();
        while (fields.hasMoreElements()) {
            name = (String)fields.nextElement();
            value = req.getHeader(name);
            currentValue = uconn.getRequestProperty(name);
            if (currentValue != null && !"".equals(currentValue)) {
                value = currentValue + ',' + value;
            }
            uconn.setRequestProperty(name, value);
        }
    }
    
    /**
     * Performs a HTTP POST redirect using the specified
     * UPFileSpec and parameters to be ignored.
     * 
     * @param req An HttpServletRequest object.     
     * @param res An HttpServletResponse object.
     * @param up the uPortal file spec.
     * @param ignoreParams An array of String objects containing
     * the parameters to be ignored.
     */
    public static void redirectPost(HttpServletRequest req,
        HttpServletResponse res, UPFileSpec up, String[] ignoreParams,
        String charset)
    throws PortalException {
        
        String parameters = buildRequestParams(req, ignoreParams);
        StringBuffer urlStr = new StringBuffer(REDIRECT_URL_LIMIT);
        String thisUri = req.getRequestURI();
    
        urlStr.append("http://").append(req.getServerName());
        urlStr.append(":").append(req.getServerPort());

        int pos = thisUri.indexOf("tag");
        if (pos >= 0) {
            urlStr.append(thisUri.substring(0, pos));
            urlStr.append(up.getUPFile());
        } else {
            log.error(
                "URLUtil::redirectPost() " +
                "Invalid url, no tag found: " + thisUri);
            throw new PortalException("Invalid URL, no tag found: " +
                thisUri);
        }
    
        if (log.isDebugEnabled())
            log.debug(
                    "URLUtil::redirectPost() " +
                    "Redirecting to framework: " + urlStr.toString());
        OutputStreamWriter wr = null;
        BufferedReader br = null;
        HttpURLConnection conn = null;
    
        try {
            URL url = new URL(urlStr.toString());
            conn = (HttpURLConnection)url.openConnection();

            conn.setDoOutput(true);
            conn.setDoInput(true);

            // forward the headers
            buildHeader(req, conn);

            // post the parameters
            wr = new OutputStreamWriter(conn.getOutputStream(), charset);
            wr.write(parameters);
            wr.flush();
            
            // now let's get the results
            conn.connect(); // throws IOException
            br = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), charset));
            StringBuffer results = new StringBuffer(512);
            String oneline;
            while ( (oneline = br.readLine()) != null) {
                results.append(oneline).append('\n');
            }
           
            // send the results back to the original requestor
            res.getWriter().print(results.toString());
        } catch (IOException ioe) {
            log.error(ioe, ioe);
            throw new PortalException(ioe);
        } finally {
			try {
				if (br != null)
					br.close();
				if (wr != null)
					wr.close();
				if (conn != null)
					conn.disconnect();
			} catch (IOException exception) {
				log.error("URLUtil:redirectPost()::Unable to close Resources "+ exception);
			}
		}
    }
}
    
