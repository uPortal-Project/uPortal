/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.container.services.information;


import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.pluto.om.window.PortletWindow;
import org.jasig.portal.ChannelManager;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.PortalException;
import org.jasig.portal.UPFileSpec;
import org.jasig.portal.UserInstance;
import org.jasig.portal.container.om.window.PortletWindowImpl;
import org.jasig.portal.layout.IUserLayout;


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
    private static final Log log = LogFactory.getLog(PortletStateManager.class);
	
    public static final String UP_PARAM_PREFIX = "uP_";
    
    // The portlet control parameter names
    public static final String ACTION = UP_PARAM_PREFIX + "portlet_action";
    public static final String MODE = "mode";
    public static final String MULTI = "multi_";
    public static final String PREV_MODE = "pmode";
    public static String PREV_STATE = "pstate";
    public static final String STATE = "state";
	
	public static final String UP_ROOT = UP_PARAM_PREFIX + "root";
	public static final String UP_TCATTR = UP_PARAM_PREFIX + "tcattr";
	public static final String UP_HELP_TARGET = UP_PARAM_PREFIX + "help_target";
	public static final String UP_EDIT_TARGET = UP_PARAM_PREFIX + "edit_target";
	public static final String UP_VIEW_TARGET = UP_PARAM_PREFIX + "view_target";
    public static final String UP_WINDOW_STATE = UP_PARAM_PREFIX + "window_state";
	public static final String MIN_CHAN_ID = UP_PARAM_PREFIX + "minimized_channelId";
	
	private static final String MINIMIZED = "minimized";
    private static final String ROOT = IUserLayout.ROOT_NODE_NAME;
	
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
	
	private static String encodeString ( String text ) {
        String result = text;
        try {
            result = URLEncoder.encode(result,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        
        return result;
	}
    
	private static String decodeString ( String text ) {
        String result;
        try {
            result = result = URLDecoder.decode(text,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        
        return result;
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
        
        //Ensure the uPFile for the window is setup appropriately for the window state
        try {
            final PortletWindowImpl windowImpl = (PortletWindowImpl)window;
            final ChannelRuntimeData runtimeData = windowImpl.getChannelRuntimeData();
            
            if (!PortalContextProviderImpl.EXCLUSIVE.equals(state)) {
                runtimeData.getUPFile().setMethod(UPFileSpec.RENDER_METHOD);
                runtimeData.getUPFile().setMethodNodeId(UserInstance.USER_LAYOUT_ROOT_NODE);
            }
        }
        catch (PortalException pe) {
        	log.error("Error setting the uPFileSpec for a non-EXCLUSIVE URL", pe);
        }            
	}


	public String toString() {
        return this.getEncodedParameterString();
    }
    
    /**
     * Generates the string representation of the request parameters for
     * the portlet based on it's current state. 
     */ 
    private String getEncodedParameterString() {
        if (windowOfAction == null)
            return "";

        StringBuffer url = new StringBuffer();

        String windowId = windowOfAction.getId().toString();

        if (nextAction)
            url.append(ACTION + "=true&");
        else
            url.append(ACTION + "=false&");

        // Window state
        if (nextState != null) {
            if (nextAction) {
                url.append(UP_WINDOW_STATE + "=" + nextState + "&");
            }
            else {
                final WindowState curState;
                if (isAction())
                    //Generating a URL during an action means the action is
                    //complete and this is the re-direct. We are actually interested
                    //in the previous WindowState which is the state the last
                    //render call took place in.
                    curState = getPrevState(windowOfAction);
                else
                    curState = getState(windowOfAction);
                
                if (!nextState.equals(curState)) {
                    
                    //Switching to MINIMIZED, Goal is not focused and minimized
                    if (WindowState.MINIMIZED.equals(nextState)) {
                        url.append(UP_TCATTR + "=" + MINIMIZED + "&");
                        url.append(MIN_CHAN_ID + "=" + windowId + "&");
                        url.append(MINIMIZED + "_" + windowId + "_value=true&");
                    }
                    //Switching to NORMAL, Goal is not focused and not minimized
                    else  if (WindowState.NORMAL.equals(nextState)) {
                        url.append(UP_ROOT + "=" + ROOT + "&");
                    }
                    //Switching to MAXIMIZED, Goal is focused and not minimized
                    else if (WindowState.MAXIMIZED.equals(nextState)) {
                        url.append(UP_ROOT + "=" + windowId + "&");
                    }
                    
                    //If our last state was minimized un-minimize it
                    if (WindowState.MINIMIZED.equals(curState) && !PortalContextProviderImpl.EXCLUSIVE.equals(nextState)) {
                        url.append(UP_TCATTR + "=" + MINIMIZED + "&");
                        url.append(MIN_CHAN_ID + "=" + windowId + "&");
                        url.append(MINIMIZED + "_" + windowId + "_value=false&");
                    }
                }
            }
        }

        //	Portlet mode
        if (nextMode != null) {

            if (nextMode.equals(PortletMode.EDIT))
                url.append(UP_EDIT_TARGET + "=" + windowId);
            else if (nextMode.equals(PortletMode.HELP))
                url.append(UP_HELP_TARGET + "=" + windowId);
            else if (nextMode.equals(PortletMode.VIEW))
                url.append(UP_VIEW_TARGET + "=" + windowId);

            url.append("&");
        }

        // Other parameters
        for (final Iterator entryItr = params.entrySet().iterator(); entryItr.hasNext(); ) {
            final Map.Entry entry = (Map.Entry)entryItr.next();
            String name = (String)entry.getKey();
            
            //Deals with parameters that start with the prefix string
            if (name.startsWith(UP_PARAM_PREFIX)) {
                name = encodeString(UP_PARAM_PREFIX + name);
            }
            else {
                name = encodeString(name);
            }
            
            final Object value = entry.getValue();
            final String[] values;
            if (value instanceof String[])
                values = (String[])value;
            else
                values = new String[] { value.toString() };
            
            for (int i = 0; i < values.length; i++) {
                url.append(name);
                url.append("=");
                url.append(encodeString(values[i]));
                url.append("&");
            }
        }

        while (url.charAt(url.length() - 1) == '&')
            url.deleteCharAt(url.length() - 1);

        for (int index = url.indexOf("&&"); index >= 0; index = url.indexOf("&&")) {
            url.replace(index, index + 2, "&");
        }
        
        return url.toString();
    } 
	
    /**
     * Returns a full portlet URL including uP file
     */
    public String getActionURL() {
        final StringBuffer actionUrl = new StringBuffer();
        final WindowState curState = getState(windowOfAction);
        
        //URLs are always absolute
        actionUrl.append(request.getContextPath());
        actionUrl.append("/");
        
        //Get the appropriate URL base
        if (PortalContextProviderImpl.EXCLUSIVE.equals(this.nextState) || (this.nextState == null && PortalContextProviderImpl.EXCLUSIVE.equals(curState))) {
            final String urlBase = runtimeData.getBaseWorkerURL(UPFileSpec.FILE_DOWNLOAD_WORKER);
            actionUrl.append(urlBase);
        }
        else {
            final String urlBase = runtimeData.getBaseActionURL();
            actionUrl.append(urlBase);
        }
        
        actionUrl.append("?");
        actionUrl.append(this.getEncodedParameterString());
        
        //Add the anchor to the URL
        if (ChannelManager.isUseAnchors()) {
            actionUrl.append("#");
            actionUrl.append(windowOfAction.getId());
        }

        return actionUrl.toString();
    }
}
