/**
 * Copyright © 2004 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package org.jasig.portal.container.services.information;


import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;
import java.net.URLDecoder;


import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.pluto.om.window.PortletWindow;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.container.om.window.PortletWindowImpl;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.utils.CommonUtils;


/**
 * The PortletStateManager implementation.
 * Analyzes the incoming request parameters for the given PortletWindow, 
 * changes the window states/portlet modes, stores them in the static hash maps, 
 * builds a portlet URL based on the changed modes/states and 
 * portlet render parameters for the current PortletWindow.
 * 
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class PortletStateManager {
	
	
    // The portlet control parameter names
    public static final String ACTION = "uP_portlet_action";
    public static final String MODE = "mode";
    public static final String MULTI = "multi_";
    public static final String PREV_MODE = "pmode";
    public static String PREV_STATE = "pstate";
    public static final String STATE = "state";
	
	public static final String UP_ROOT = "uP_root";
	public static final String UP_TCATTR = "uP_tcattr";
	public static final String UP_HELP_TARGET = "uP_help_target";
	public static final String UP_EDIT_TARGET = "uP_edit_target";
	public static final String UP_VIEW_TARGET = "uP_view_target";
	public static final String MIN_CHAN_ID = "minimized_channelId";
	
	private static final String ENC_SEP = "~~%%~~";
	private static final String MINIMIZED = "minimized";
	private static final String ROOT = "root";
	
	private Map params = new HashMap();


    // The following maps store the window states 
    // and portlet modes, get cleared at the end of
    // the user session
	private static Map windowStates = new HashMap();
	private static Map portletModes = new HashMap();
	
	private PortletWindowImpl windowOfAction;
	private ChannelRuntimeData runtimeData;
	private HttpServletRequest request;
	
	// Indicates the current action
	private boolean isAction;
	// Indicates the action of the next request
	private boolean nextAction;
	
	private PortletMode nextMode;
	private WindowState nextState;
	
	
    
	public PortletStateManager ( PortletWindow window ) {
	  this.windowOfAction = (PortletWindowImpl) window;
	  nextMode = null;
	  nextState = null;
	  isAction = nextAction = false;
	  request = windowOfAction.getHttpServletRequest();
	  runtimeData = windowOfAction.getChannelRuntimeData();
	  if ( windowOfAction != null && runtimeData != null && request != null )
		analyzeRequestInformation();
	}
	
	public static synchronized Hashtable getURLDecodedParameters ( HttpServletRequest request ) {
		String url = request.getRequestURL().toString();
		if ( url.indexOf(UPFileSpec.PORTLET_PARAMS_DELIM_BEG) > 0 ) {
		  int offset = UPFileSpec.PORTLET_PARAMS_DELIM_BEG.length();	
			 String encodedParams = url.substring(url.indexOf(UPFileSpec.PORTLET_PARAMS_DELIM_BEG)+offset,
									url.indexOf(UPFileSpec.PORTLET_PARAMS_DELIM_END));	
			return decodeURLParameters(URLDecoder.decode(encodedParams));
		}	
			return new Hashtable();	
	}
	
	/**
	  * Sets the next portlet mode for the current PortletWindow
	  * @param mode a portlet mode
      */
	public void setNextMode(PortletMode mode) {
	  nextMode = mode;	
	}
	
	/**
	  * Sets the next window state for the current PortletWindow
	  * @param state a window state
	  */
	public void setNextState(WindowState state) {
	  nextState = state;	
	}
	
	/**
	  * Setting the portlet action parameter for the next request 
	  *
	  */
	public void setAction() {
		nextAction = true;
	}
	
	
	/**
	  * Analyzes the request parameters and sets portlet modes/window states for the current PortletWindow
	  */
    private void analyzeRequestInformation() {
        params.clear();
        String windowId = windowOfAction.getId().toString();
        for (Iterator i = runtimeData.getParameters().keySet().iterator(); i.hasNext();) {
            String paramName = (String)i.next();
            String[] values = runtimeData.getParameterValues(paramName);
        
            if (ACTION.equals(paramName)) {
                if ("true".equals(values[0]))
                    isAction = true;
            } else if (UP_HELP_TARGET.equals(paramName) && windowId.equals(values[0])) {
                setMode(windowOfAction, PortletMode.HELP);
            } else if (UP_EDIT_TARGET.equals(paramName) && windowId.equals(values[0])) {
                setMode(windowOfAction, PortletMode.EDIT);
            } else if (UP_VIEW_TARGET.equals(paramName) && windowId.equals(values[0])) {
                setMode(windowOfAction, PortletMode.VIEW);
            } else if (UP_ROOT.equals(paramName)) {
                if (!ROOT.equals(values[0]))
                    setState(windowOfAction, WindowState.MAXIMIZED);
                else if (getPrevState(windowOfAction).equals(WindowState.MAXIMIZED))
                    setState(windowOfAction, WindowState.NORMAL);
            } else if (UP_TCATTR.equals(paramName)) {
                if (MINIMIZED.equals(values[0])) {
                    String state = runtimeData.getParameter(MINIMIZED + "_" + windowId + "_value");
                    if ("true".equals(state))
                        setState(windowOfAction, WindowState.MINIMIZED);
                    else
                        setState(windowOfAction, WindowState.NORMAL);
                }
            }
        }
    }
	
	/**
	  * Adds the render parameters to the portlet URL
	  * @param parameters a <code>Map</code> containing the render parameters
	  */
	public void setParameters(Map parameters) {	
	  if ( parameters != null && !parameters.isEmpty() )	
	   params.putAll(parameters);
	}

	/**
	  * Returns true if the current PortletRequest is ActionRequest,
	  * false - otherwise
	  */
    public boolean isAction() {
      return isAction;
    }

	private static String decodeMultiName( PortletWindow window, String sessionId, String paramName) {
		return paramName.substring((getKey(window)+MULTI).length());
	}
	
	private static String encodeMultiName( PortletWindow window, String sessionId, String paramName) {
		return getKey(window) + MULTI + paramName;
	}

	private static String encodeValues( String values[] ) {
	   String value = ENC_SEP;
	   for ( int i = 0; i < values.length; i++ )
	     value += values[i] + ENC_SEP;
	   return value;  
	}

    private static String[] decodeValues( String value ) {
	   StringTokenizer tokenizer = new StringTokenizer(value, ENC_SEP);
	   String[] values = new String[tokenizer.countTokens()];
	   for ( int i = 0; i < values.length && tokenizer.hasMoreTokens(); i++ )
		 values[i] = tokenizer.nextToken();
	   return values;  
	}
	
	
	private static String encodeQueryString ( String text ) {
		String result = CommonUtils.replaceText(text, "&", "_and_");
		result = CommonUtils.replaceText(result, "=", "_eq_");
		result = CommonUtils.replaceText(result, UPFileSpec.PORTAL_URL_SEPARATOR, "__"); 
		return result;
	}


	private static String decodeQueryString ( String text ) {
		String result = CommonUtils.replaceText(text, "_and_", "&");
		result = CommonUtils.replaceText(result, "_eq_", "=");
		result = CommonUtils.replaceText(result, "__", UPFileSpec.PORTAL_URL_SEPARATOR);
		return result;
	}		
	
	
	public static Hashtable decodeURLParameters ( String encodedParameters ) {
	  Hashtable params = new Hashtable();		
	  if ( encodedParameters == null || encodedParameters.length() <= 0 )
	    return params;	
	  StringTokenizer tokenizer = new StringTokenizer(decodeQueryString(encodedParameters),"&");
	  while ( tokenizer.hasMoreTokens() ) {
	  	String param = tokenizer.nextToken();
	  	String paramName = param.substring(0,param.indexOf('='));
	  	String paramValue = param.substring(param.indexOf("=")+1);
	  	if ( params.containsKey(paramName) ){
	  	  Vector values = (Vector) params.get(paramName);
	  	  values.add(paramValue);	
	  	} else {
	  	   Vector values = new Vector();
	  	   values.add(paramValue);	
	  	   params.put(paramName,values);
	  	}
	  }
	  for ( Iterator i = params.keySet().iterator(); i.hasNext(); ) {
	  	Object key = i.next();
	  	Vector values = (Vector) params.get(key);
	  	int size = values.size();
	  	String[] strValues = new String[size];
	  	for ( int j = 0; j < size; j++ )
	  	 strValues[j] = (String) values.get(j);
	  	params.put(key,strValues);
	  }
	    return params;   
	}
	
	
	public static String encodeURLParameters ( String urlParameters ) {
		if ( urlParameters == null || urlParameters.length() <= 0 )
		  return "";
		return encodeQueryString(urlParameters);
	}	
		
	
	/**
	  * Generates the hash key for the given PortletWindow
	  * based on the user session ID and portlet window ID
	  * @param window a portlet window
	  * @return a String hash key
	  */ 
	public static String getKey(PortletWindow window) {
		    PortletWindowImpl windowImpl = (PortletWindowImpl) window; 
		    HttpSession session = windowImpl.getHttpServletRequest().getSession();
		    String sessionId = (session!=null)?session.getId():null;
			return ((sessionId!=null)?sessionId+"_":"")+window.getId().toString()+"_"; 
	}
	
	
	/**
	  * Clears the states/modes of the given PortletWindow.
	  * @param portletWindow a portlet window
	  */  
	public static void clearState(PortletWindow portletWindow) {
	  Map map = windowStates;	
	  for ( int i = 0; i < 2; i++ )	{
		Iterator keyIterator = map.keySet().iterator();
		while ( keyIterator.hasNext() ) {
			String name = (String) keyIterator.next();
			if (name.startsWith(getKey(portletWindow))) {
				keyIterator.remove();
			}
		}
		 map = portletModes;
	  }	
	     
	}
	
	/**
	  * Clears the states/modes for the porlet windows
	  * associated with the given HttpSession
	  * @param request a <code>HttpServletRequest</code> instance
	  */  
	public static void clearState( HttpServletRequest request ) {
	 HttpSession session = request.getSession();	
	 if ( session == null ) 
	 	return;
	 Map map = windowStates;	
	 for ( int i = 0; i < 2; i++ )	{
	  Iterator keyIterator = map.keySet().iterator();
	  while ( keyIterator.hasNext() ) {
		  String name = (String) keyIterator.next();
		  
		  if (name.startsWith(session.getId())) {
			  keyIterator.remove();
		  }
	  }
	   map = portletModes;
	 }	
	}

	/**
	  * Clears the render parameters for the current PortletWindow
	  */  	
	public void clearParameters() {
	  params.clear();	  
	}
	
	
	/**
	  * Returns the current portlet mode for the given PortletWindow
	  * @param window a portlet window
	  * @return a PortletMode instance
	  */  
	public static PortletMode getMode(PortletWindow window) {
		String mode =
			(String) portletModes.get(getKey(window)+MODE);
		if (mode != null)
			return new PortletMode(mode);
		else
			return PortletMode.VIEW;
	}

	/**
	  * Returns the previous portlet mode for the given PortletWindow
	  * @param window a portlet window
	  * @return a PortletMode instance
	  */  
	public static PortletMode getPrevMode(PortletWindow window) {
		String mode =
			(String) portletModes.get(getKey(window)+PREV_MODE);
		if (mode != null)
			return new PortletMode(mode);
		else
			return PortletMode.VIEW;
	}
	
	
	/**
	  * Returns the current portlet state for the given PortletWindow
	  * @param window a portlet window
	  * @return a WindowState instance
	  */  
	public static WindowState getState(PortletWindow window) {
			String state =
				(String) windowStates.get(getKey(window)+STATE);
			if (state != null)
				return new WindowState(state);
			else
				return WindowState.NORMAL;
	}

	/**
	  * Returns the previous portlet state for the given PortletWindow
	  * @param window a portlet window
	  * @return a WindowState instance
	  */ 
	public static WindowState getPrevState(PortletWindow window) {
		String state =
			(String) windowStates.get(getKey(window)+PREV_STATE);
		if (state != null)
			return new WindowState(state);
		else
			return WindowState.NORMAL;
	}


	/**
	  * Sets the portlet mode for the given PortletWindow
	  * @param window a portlet window
	  * @param mode a portlet mode
	  */ 
	public static void setMode(PortletWindow window, PortletMode mode) {
		PortletWindowImpl windowImpl = (PortletWindowImpl) window;
		Object prevMode = portletModes.get(getKey(window)+MODE);
		if (prevMode != null)
		  portletModes.put(getKey(window)+PREV_MODE, prevMode);
		// set current mode
		portletModes.put(getKey(window)+MODE, mode.toString() );
	}

	/**
	  * Sets the window state for the given PortletWindow
	  * @param window a portlet window
	  * @param state a window state
	  */ 
	public static void setState(PortletWindow window, WindowState state) {
		Object prevState = windowStates.get(getKey(window)+STATE);
		if (prevState != null)
		  windowStates.put(getKey(window)+PREV_STATE, prevState);
		// set current state
	    windowStates.put(getKey(window)+STATE, state.toString() );
	}


	/**
	  * Generates the string representation of the portlet URL based on the 
	  * next/current portlet modes/states and render parameters for the current
	  * PortletWindow
	  */ 
	public String toString() {
		
		if ( windowOfAction == null ) return "";

		StringBuffer url = new StringBuffer();
		
		String windowId = windowOfAction.getId().toString();
		
        if ( nextAction )
            url.append(ACTION+"=true&");
        else
            url.append(ACTION+"=false&");
		
		// Window state
		if ( nextState != null ) {
			
		 WindowState curState = getState(windowOfAction);
				
		 if ( nextState.equals(WindowState.MINIMIZED) )
		  url.append(UP_TCATTR+"="+MINIMIZED+"&"+MIN_CHAN_ID+"="+windowId+"&"+MINIMIZED+"_"+windowId+"_value=true");
		 else if ( nextState.equals(WindowState.NORMAL) && curState.equals(WindowState.MINIMIZED) )
		  url.append(UP_TCATTR+"="+MINIMIZED+"&"+MIN_CHAN_ID+"="+windowId+"&"+MINIMIZED+"_"+windowId+"_value=false");
		 else if ( nextState.equals(WindowState.NORMAL) && curState.equals(WindowState.MAXIMIZED) )
		  url.append(UP_ROOT+"="+ROOT);
		 else if ( nextState.equals(WindowState.MAXIMIZED) )
		  url.append(UP_ROOT+"="+windowId); 
		  
		  url.append("&");  
		}  
		  
		
		  
        //	Portlet mode
        if ( nextMode != null ) {
		
		  if ( nextMode.equals(PortletMode.EDIT) )
		     url.append(UP_EDIT_TARGET+"="+windowId);  
		  else if ( nextMode.equals(PortletMode.HELP) ) 
		     url.append(UP_HELP_TARGET+"="+windowId); 
		  else if ( nextMode.equals(PortletMode.VIEW) ) 
			 url.append(UP_VIEW_TARGET+"="+windowId);  	    	   
	   

		  url.append("&");   
        }
        
        
        // Other parameters  
		Iterator keys = params.keySet().iterator();
		while ( keys.hasNext() ) {
			String name = (String) keys.next();
			Object value = params.get(name);
            String[] values = (value instanceof String[]) ? (String[]) value : new String[] {value.toString()};
			for ( int i = 0; i < values.length; i++ ) {
				 url.append(name).append("=").append(values[i]);
				 url.append("&");
			} 
		}
   
        String strURL = url.toString();
        while ( strURL.endsWith("&") )
         strURL = strURL.substring(0,strURL.lastIndexOf("&"));
		
		return strURL;
	} 
	
	public String getActionURL() {
		String baseActionURL;
		if ( nextState != null && nextState.equals(PortalContextProviderImpl.EXCLUSIVE) ) {
		    baseActionURL = runtimeData.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER);
		}        
		else {
		    baseActionURL = runtimeData.getBaseActionURL();
		}
        
		String encodedURLParams = encodeURLParameters(this.toString());
        StringBuffer url = new StringBuffer((encodedURLParams.length()>0)?
				 (UPFileSpec.PORTLET_PARAMS_DELIM_BEG+java.net.URLEncoder.encode(encodedURLParams)+
				  UPFileSpec.PORTLET_PARAMS_DELIM_END+
				  UPFileSpec.PORTAL_URL_SEPARATOR+baseActionURL):baseActionURL);
		return url.toString();
	}
}
