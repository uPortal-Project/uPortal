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

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;

import org.apache.pluto.om.window.PortletWindow;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.container.om.window.PortletWindowImpl;


/**
 * Implementation of Portlet URL Manager
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class PortletStateManager {

	static public final String ACTION = "P_JSR168_ACT";
	static public final String MODE = "mode";
	static public final String MULTI = "multi_";
	static public final String PREV_MODE = "pmode";
	static public final String PREV_STATE = "pstate";
	static public final String STATE = "state";
	
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

	private static Map windowStates = new HashMap();
	private static Map portletModes = new HashMap();
	
	private PortletWindowImpl windowOfAction;
	private ChannelRuntimeData runtimeData;
	private HttpServletRequest request;
	
	// Indicates the current action
	private boolean isAction;
	// Indicate the action of the next request
	private boolean nextAction;
	
	private PortletMode nextMode;
	private WindowState nextState;
	
	public void setNextMode(PortletMode mode) {
	  nextMode = mode;	
	}
	
	public void setNextState(WindowState state) {
	  nextState = state;	
	}
	
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
	
	public void setAction() {
		nextAction = true;
	}
	
    private void analyzeRequestInformation() {
        params.clear();
        String windowId = windowOfAction.getId().toString();
        for (Enumeration names = runtimeData.getParameterNames(); names.hasMoreElements();) {
            String paramName = (String)names.nextElement();
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
	
	public void setParameters(Map parameters) {	
	  if ( parameters != null && !parameters.isEmpty() )	
	   params.putAll(parameters);
	}

    public boolean isAction() {
      return isAction;
    }

	private static String decodeMultiName( PortletWindow window, String sessionId, String paramName) {
		return paramName.substring((getKey(window)+MULTI).length());
	}
	
	private static String encodeMultiName( PortletWindow window, String sessionId, String paramName) {
		return getKey(window) + MULTI + paramName;
	}

	public static String encodeValues( String values[] ) {
	   String value = ENC_SEP;
	   for ( int i = 0; i < values.length; i++ )
	     value += values[i] + ENC_SEP;
	   return value;  
	}

    public static String[] decodeValues( String value ) {
	   StringTokenizer tokenizer = new StringTokenizer(value, ENC_SEP);
	   String[] values = new String[tokenizer.countTokens()];
	   for ( int i = 0; i < values.length && tokenizer.hasMoreTokens(); i++ )
		 values[i] = tokenizer.nextToken();
	   return values;  
	}

	public static String getKey(PortletWindow window) {
		    PortletWindowImpl windowImpl = (PortletWindowImpl) window; 
		    String sessionId = windowImpl.getHttpServletRequest().getSession().getId();
			return ((sessionId!=null)?sessionId+"_":"")+window.getId().toString()+"_"; 
	}

	public static void clearParameters(PortletWindow portletWindow) {
	  Map map = windowStates;	
	  for ( int i = 0; i < 2; i++ )	{
		Iterator keyIterator = map.keySet().iterator();
		while ( keyIterator.hasNext() ) {
			String name = (String) keyIterator.next();
			if (name.startsWith(getKey(portletWindow))) {
				keyIterator.remove();
			}
		}
		if ( i == 1 )
		 map = portletModes;
	  }	
	     
	}
	
	public static void clearState( HttpServletRequest request ) {
		Map map = windowStates;	
	 for ( int i = 0; i < 2; i++ )	{
	  Iterator keyIterator = map.keySet().iterator();
	  while ( keyIterator.hasNext() ) {
		  String name = (String) keyIterator.next();
		  if (name.startsWith(request.getSession().getId())) {
			  keyIterator.remove();
		  }
	  }
	  if ( i == 1 )
	   map = portletModes;
	 }	
	}

	
	public void clearParameters() {
	  params.clear();	  
	}

	public static PortletMode getMode(PortletWindow window) {
		String mode =
			(String) portletModes.get(getKey(window)+MODE);
		if (mode != null)
			return new PortletMode(mode);
		else
			return PortletMode.VIEW;
	}

	public static PortletMode getPrevMode(PortletWindow window) {
		String mode =
			(String) portletModes.get(getKey(window)+PREV_MODE);
		if (mode != null)
			return new PortletMode(mode);
		else
			return PortletMode.VIEW;
	}
	
	public static WindowState getState(PortletWindow window) {
			String state =
				(String) windowStates.get(getKey(window)+STATE);
			if (state != null)
				return new WindowState(state);
			else
				return WindowState.NORMAL;
	}

	public static WindowState getPrevState(PortletWindow window) {
		String state =
			(String) windowStates.get(getKey(window)+PREV_STATE);
		if (state != null)
			return new WindowState(state);
		else
			return WindowState.NORMAL;
	}


	public static void setMode(PortletWindow window, PortletMode mode) {
		PortletWindowImpl windowImpl = (PortletWindowImpl) window;
		Object prevMode = portletModes.get(getKey(window)+MODE);
		if (prevMode != null)
		  portletModes.put(getKey(window)+PREV_MODE, prevMode);
		// set current mode
		portletModes.put(getKey(window)+MODE, mode.toString() );
	}

	public static void setState(PortletWindow window, WindowState state) {
		Object prevState = windowStates.get(getKey(window)+STATE);
		if (prevState != null)
		  windowStates.put(getKey(window)+PREV_STATE, prevState);
		// set current state
	    windowStates.put(getKey(window)+STATE, state.toString() );
	}

	public String toString() {
		
		if ( windowOfAction == null ) return "";

		StringBuffer url = new StringBuffer();
		
		String windowId = windowOfAction.getId().toString();
		
		if ( nextAction )
		  url.append(ACTION+"=true&");
		
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
		if ( strURL.endsWith("&") )
		 strURL = strURL.substring(0,strURL.lastIndexOf("&"));
		
		return strURL;
	} 
}
