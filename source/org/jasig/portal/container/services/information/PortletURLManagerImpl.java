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
import java.util.Enumeration;
import java.util.StringTokenizer;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import org.apache.pluto.om.window.PortletWindow;


import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.container.om.window.PortletWindowImpl;


/**
 * Implementation of Portlet URL Manager
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class PortletURLManagerImpl implements PortletURLManager {

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
	public static final String UP_ABOUT_TARGET = "uP_about_target";
	public static final String MIN_CHAN_ID = "minimized_channelId";
	
	private static final String ENC_SEP = "~~%%~~";
	private static final String MINIMIZED = "minimized";
	private static final String ROOT = "root";
	
	private static HashMap windowStates = new HashMap();
	private static HashMap portletModes = new HashMap();
	private HashMap params;
	
	private PortletWindow windowOfAction;
	private ChannelRuntimeData runtimeData;
	private boolean isAction;
	
	
	public void setWindowOfAction ( PortletWindow windowOfAction ) {
	  //System.out.println ( "window of action: " + windowOfAction.getId().toString() + " runtime data: " + runtimeData );
	  this.windowOfAction = windowOfAction;
	  isAction = false;
	  runtimeData = ((PortletWindowImpl)windowOfAction).getChannelRuntimeData();
	  if ( windowOfAction != null && runtimeData != null )
		analizeRequestInformation();
	}
	
	public void setAction() {
		isAction = true;
	}
	
	private void analizeRequestInformation() {
		params = new HashMap();
		for (Enumeration names = runtimeData.getParameterNames(); names.hasMoreElements();) {
		  String paramName = (String) names.nextElement();
		  String[] values = runtimeData.getParameterValues(paramName);
		  
		  if ( ACTION.equals(paramName) ) {
		    if ( "true".equals(values[0]) )
		      isAction = true;		  
		  } else if ( UP_ROOT.equals(paramName) ) {
		     if ( !ROOT.equals(values[0]) )
		      setState ( windowOfAction, WindowState.MAXIMIZED); 
		     else if ( getPrevState(windowOfAction).equals(WindowState.MAXIMIZED) )
			  setState ( windowOfAction, WindowState.NORMAL ); 			   
		  }	else if ( UP_TCATTR.equals(paramName) ) {
		      if ( MINIMIZED.equals(values[0]) ) {
		        String state = runtimeData.getParameter(MINIMIZED+"_"+getKey(windowOfAction)+"_value");
		        if ( "true".equals(state) )
				 setState ( windowOfAction, WindowState.MINIMIZED );
				else
				 setState ( windowOfAction, WindowState.NORMAL ); 
		      }	  		     
		    } else {
		       if ( values.length > 0 )	
		        params.put(encodeMultiName(windowOfAction,paramName),encodeValues(values));
		       else  
			    params.put(paramName,values[0]);
		      }    
		}  
	}

    public boolean isAction() {
      return isAction;
    }

	private static String decodeMultiName( PortletWindow window, String paramName) {
		return paramName.substring((getKey(window)+MULTI).length());
	}
	
	private static String encodeMultiName( PortletWindow window, String paramName) {
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
			return window.getId().toString() + "_"; 
	}

	public void clearParameters(PortletWindow portletWindow) {
	  HashMap map = windowStates;	
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
	     //if ( params != null ) params.clear();
	}
	
	public void clearParameters() {
	  if ( params != null ) params.clear();	  
	}

	public PortletMode getMode(PortletWindow window) {
		String mode =
			(String) portletModes.get(getKey(window)+MODE);
		if (mode != null)
			return new PortletMode(mode);
		else
			return PortletMode.VIEW;
	}

	public PortletMode getPrevMode(PortletWindow window) {
		String mode =
			(String) portletModes.get(getKey(window)+PREV_MODE);
		if (mode != null)
			return new PortletMode(mode);
		else
			return PortletMode.VIEW;
	}
	
	public WindowState getState(PortletWindow window) {
			String state =
				(String) windowStates.get(getKey(window)+STATE);
			if (state != null)
				return new WindowState(state);
			else
				return WindowState.NORMAL;
	}

	public WindowState getPrevState(PortletWindow window) {
		String state =
			(String) windowStates.get(getKey(window)+PREV_STATE);
		if (state != null)
			return new WindowState(state);
		else
			return WindowState.NORMAL;
	}


	public void setMode(PortletWindow window, PortletMode mode) {
		Object prevMode = portletModes.get(getKey(window)+MODE);
		if (prevMode != null)
		  portletModes.put(getKey(window)+PREV_MODE, prevMode);
		// set current mode
		portletModes.put(getKey(window)+MODE, mode.toString() );
	}

	public void setState(PortletWindow window, WindowState state) {
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
		
		if ( isAction() )
		  url.append(ACTION+"=true");
		  
		int size = url.length();  
		
		// Window state
		WindowState state = getState(windowOfAction);
		WindowState prevState = getState(windowOfAction);
				
		if ( state.equals(WindowState.MINIMIZED) )
		  url.append((size>0?"&":"")+UP_TCATTR+"="+MINIMIZED+"&"+MIN_CHAN_ID+"="+windowId+"&"+MINIMIZED+"_"+windowId+"_value=true");
		else if ( state.equals(WindowState.NORMAL) && prevState.equals(WindowState.MINIMIZED) )
		  url.append((size>0?"&":"")+UP_TCATTR+"="+MINIMIZED+"&"+MIN_CHAN_ID+"="+windowId+"&"+MINIMIZED+"_"+windowId+"_value=false");
		else if ( state.equals(WindowState.NORMAL) && prevState.equals(WindowState.MAXIMIZED) )
		  url.append((size>0?"&":"")+UP_ROOT+"="+ROOT);
		else if ( state.equals(WindowState.MAXIMIZED) )
		  url.append((size>0?"&":"")+UP_ROOT+"="+windowId); 
		  
        //	Portlet mode
		PortletMode mode = getMode(windowOfAction);  
		size = url.length();
		
		if ( mode.equals(PortletMode.EDIT) )
			url.append((size>0?"&":"")+UP_EDIT_TARGET+"="+windowId);  
		else if ( mode.equals(PortletMode.HELP) ) 
		    url.append((size>0?"&":"")+UP_HELP_TARGET+"="+windowId);  	   

	    // Other parameters
		if ( url.length() > 0 ) url.append("&");
		Iterator keys = params.keySet().iterator();
		while ( keys.hasNext() ) {
			String name = (String) keys.next();
			if (name.startsWith(getKey(windowOfAction))) {
			 String value = (String) params.get(name);
			 if ( name.indexOf(MULTI) > 0 ) {
			    String[] values = decodeValues(value);
			    for ( int i = 0; i < values.length; i++ ) {
				 url.append(decodeMultiName(windowOfAction,name)).append("=").append(values[i]);
				 url.append("&");
			    } 
			 } else {
			     url.append(decodeMultiName(windowOfAction,name)).append("=").append((String)value); 
			     url.append("&");   
			   }    	 
			}
		}

        
		String strURL = url.toString();
		if ( strURL.endsWith("&") )
		 strURL = strURL.substring(0,strURL.lastIndexOf("&"));
		
		return strURL;
	} 
}
