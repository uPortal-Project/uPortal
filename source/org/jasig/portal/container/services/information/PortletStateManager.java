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
import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
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
    private static final Log LOG = LogFactory.getLog(PortletStateManager.class);

    public static final String UP_PARAM_PREFIX = "uP_";

    // The portlet control parameter names
    public static final String ACTION =             UP_PARAM_PREFIX + "portlet_action";
    public static final String UP_ROOT =            UP_PARAM_PREFIX + "root";
    public static final String UP_TCATTR =          UP_PARAM_PREFIX + "tcattr";
    public static final String UP_HELP_TARGET =     UP_PARAM_PREFIX + "help_target";
    public static final String UP_EDIT_TARGET =     UP_PARAM_PREFIX + "edit_target";
    public static final String UP_VIEW_TARGET =     UP_PARAM_PREFIX + "view_target";
    public static final String UP_WINDOW_STATE =    UP_PARAM_PREFIX + "window_state";
    public static final String MIN_CHAN_ID =        "minimized_channelId";

    private static final String MINIMIZED = "minimized";
    private static final String ROOT =      IUserLayout.ROOT_NODE_NAME;

    private final PortletWindowImpl windowOfAction;

    //** Fields for URL generation
    // Indicates the current action
    private boolean isAction;
    // Indicates the action of the next request
    private boolean nextAction;
    private PortletMode nextMode;
    private WindowState nextState;

    private final Map params = new HashMap();

    /**
     * Creates a new PortletStateManager instance which can be used for
     * generating a URL for the specified PortletWindow.
     * 
     * @param window The PortletWindow to generate a URL for.
     */
    public PortletStateManager(PortletWindow window) {
        if (window == null)
            throw new IllegalArgumentException("window cannot be null");
        
        this.windowOfAction = (PortletWindowImpl)window;
        this.nextMode = null;
        this.nextState = null;
        this.isAction = false;
        this.nextAction = false;
        
        if (windowOfAction.getChannelRuntimeData() != null && windowOfAction.getHttpServletRequest() != null)
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
     */
    public void setAction() {
        nextAction = true;
    }
    
    /**
     * Adds the render parameters to the portlet URL
     * @param parameters a <code>Map</code> containing the render parameters
     */
    public void setParameters(Map parameters) {
        if (parameters != null && !parameters.isEmpty())
            params.putAll(parameters);
    }

    /**
     * Returns true if the current PortletRequest is ActionRequest,
     * false - otherwise
     */
    public boolean isAction() {
        return isAction;
    }    

    /**
     * Clears the render parameters for the current PortletWindow
     */
    public void clearParameters() {
        params.clear();
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString() {
        return this.getEncodedParameterString();
    }

    /**
     * Returns a full portlet URL including uP file
     */
    public String getActionURL() {
        final StringBuffer actionUrl = new StringBuffer();
        final WindowState curState = getState(this.windowOfAction);
        final HttpServletRequest request = this.windowOfAction.getHttpServletRequest();
        final ChannelRuntimeData runtimeData = this.windowOfAction.getChannelRuntimeData();

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
    
    /**
     * Generates the string representation of the request parameters for
     * the portlet based on it's current state. 
     */
    private String getEncodedParameterString() {
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
                    else if (WindowState.NORMAL.equals(nextState)) {
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

        //  Portlet mode
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
        for (final Iterator entryItr = params.entrySet().iterator(); entryItr.hasNext();) {
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

                if (values[i] != null)
                    url.append(encodeString(values[i]));

                url.append("&");
            }
        }

        while (url.charAt(url.length() - 1) == '&') {
            url.deleteCharAt(url.length() - 1);
        }

        for (int index = url.indexOf("&&"); index >= 0; index = url.indexOf("&&")) {
            url.deleteCharAt(index);
        }

        return url.toString();
    }

    /**
     * Analyzes the request parameters and sets portlet modes/window states for the current PortletWindow
     */
    private void analyzeRequestInformation() {
        this.params.clear();
        final String windowId = this.windowOfAction.getId().toString();
        
        final ChannelRuntimeData runtimeData = this.windowOfAction.getChannelRuntimeData();
        for (Iterator i = runtimeData.getParameters().keySet().iterator(); i.hasNext();) {
            String paramName = (String)i.next();
            String[] values = runtimeData.getParameterValues(paramName);

            if (ACTION.equals(paramName)) {
                isAction = new Boolean(values[0]).booleanValue();
            }
            else if (UP_HELP_TARGET.equals(paramName) && windowId.equals(values[0])) {
                setMode(windowOfAction, PortletMode.HELP);
            }
            else if (UP_EDIT_TARGET.equals(paramName) && windowId.equals(values[0])) {
                setMode(windowOfAction, PortletMode.EDIT);
            }
            else if (UP_VIEW_TARGET.equals(paramName) && windowId.equals(values[0])) {
                setMode(windowOfAction, PortletMode.VIEW);
            }
            else if (UP_ROOT.equals(paramName)) {
                if (!ROOT.equals(values[0]))
                    setState(windowOfAction, WindowState.MAXIMIZED);
                else if (getPrevState(windowOfAction).equals(WindowState.MAXIMIZED))
                    setState(windowOfAction, WindowState.NORMAL);
            }
            else if (UP_TCATTR.equals(paramName)) {
                if (MINIMIZED.equals(values[0])) {
                    String state = runtimeData.getParameter(MINIMIZED + "_" + windowId + "_value");
                    if (new Boolean(state).booleanValue())
                        setState(windowOfAction, WindowState.MINIMIZED);
                    else
                        setState(windowOfAction, WindowState.NORMAL);
                }
            }
        }
    }
    
    /**
     * Generates the UTF-8 URL encoded version of the string, wrapping the 
     * possible UnsupportedEncodingException in a RuntimeException.
     * 
     * @param text The string to encode.
     * @return The encoded version of the string.
     */
    private String encodeString(String text) {
        try {
            return URLEncoder.encode(text, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            LOG.error("Error URL encoding string to 'UTF-8'", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * Generates the UTF-8 URL decoded version of the string, wrapping the 
     * possible UnsupportedEncodingException in a RuntimeException.
     * 
     * @param text The string to decode.
     * @return The decoded version of the string.
     */
    private String decodeString(String text) {
        try {
            return URLDecoder.decode(text, "UTF-8");
        }
        catch (UnsupportedEncodingException e) {
            LOG.error("Error URL decoding string to 'UTF-8'", e);
            throw new RuntimeException(e);
        }
    }
    
    
    /**
     * Clears the PorletMode and WindowState information for
     * the specifid PortletWindow
     * 
     * @param window The PortletWindow to clear the state information for
     */
    public static void clearState(PortletWindow window) {
        final HttpSession session = getSession(window);

        if (session != null)
            session.removeAttribute(getKey(window));
    }

    /**
     * Returns the current portlet mode for the given PortletWindow
     * 
     * @param window a portlet window
     * @return a PortletMode instance
     */
    public static PortletMode getMode(PortletWindow window) {
        final PortletWindowStateInfo stateInfo = getStateInfo(window);
        return stateInfo.getCurrentMode();
    }

    /**
     * Returns the previous portlet mode for the given PortletWindow
     * 
     * @param window a portlet window
     * @return a PortletMode instance
     */
    public static PortletMode getPrevMode(PortletWindow window) {
        final PortletWindowStateInfo stateInfo = getStateInfo(window);
        return stateInfo.getPreviousMode();
    }

    /**
     * Returns the current portlet state for the given PortletWindow
     * 
     * @param window a portlet window
     * @return a WindowState instance
     */
    public static WindowState getState(PortletWindow window) {
        final PortletWindowStateInfo stateInfo = getStateInfo(window);
        return stateInfo.getCurrentState();
    }

    /**
     * Returns the previous portlet state for the given PortletWindow
     * 
     * @param window a portlet window
     * @return a WindowState instance
     */
    public static WindowState getPrevState(PortletWindow window) {
        final PortletWindowStateInfo stateInfo = getStateInfo(window);
        return stateInfo.getPreviousState();
    }

    /**
     * Sets the portlet mode for the given PortletWindow
     * 
     * @param window a portlet window
     * @param mode a portlet mode
     */
    public static void setMode(PortletWindow window, PortletMode mode) {
        final PortletWindowStateInfo stateInfo = getStateInfo(window);
        stateInfo.setCurrentMode(mode);
    }

    /**
     * Sets the window state for the given PortletWindow
     * 
     * @param window a portlet window
     * @param state a window state
     */
    public static void setState(PortletWindow window, WindowState state) {
        final PortletWindowStateInfo stateInfo = getStateInfo(window);
        stateInfo.setCurrentState(state);

        //Ensure the uPFile for the window is setup appropriately for the window state
        try {
            final PortletWindowImpl windowImpl = (PortletWindowImpl)window;
            final ChannelRuntimeData runtimeData = windowImpl.getChannelRuntimeData();

            if (!PortalContextProviderImpl.EXCLUSIVE.equals(stateInfo.getCurrentState())) {
                runtimeData.getUPFile().setMethod(UPFileSpec.RENDER_METHOD);
                runtimeData.getUPFile().setMethodNodeId(UserInstance.USER_LAYOUT_ROOT_NODE);
            }
        }
        catch (PortalException pe) {
            LOG.error("Error setting the uPFileSpec for a non-EXCLUSIVE URL", pe);
        }
    }


    /**
     * Gets a HttpSession for the specified PortletWindow. Will create one
     * if needed.
     * 
     * @param window The PortletWindow to get the HttpSession for.
     * @return The HttpSession for the PortletWindow.
     * @throws IllegalStateException If the PortletWindow doesn't have an associated HttpServletRequest object.
     */
    private static HttpSession getSession(PortletWindow window) {
        final PortletWindowImpl windowImpl = (PortletWindowImpl)window;
        final HttpServletRequest request = windowImpl.getHttpServletRequest();
        
        /*
         * Unwrap the HttpServletRequest!
         * When this is getting called via the dispatched portlet it gets
         * a HttpServletRequestWrapper generated by the cross context
         * dispatch. If you dig deep enough in the HttpServletRequestWrapper
         * layers you can find the original request which is needed here to
         * ensure the correct session is returned.
         */
        final HttpServletRequest realRequest = getUnwrapedRequest(request);

        if (realRequest != null) {
            return realRequest.getSession(true);
        }
        else {
            throw new IllegalStateException("No HttpServletRequest could be found for PortletWindow.id='" + window.getId() + "'");
        }
    }
    
    /**
     * Recursivly unwrapps a HttpServletRequest. Determines if the current object
     * implements HttpServletRequestWrapper, gets the wrapped request and tries to
     * un wrap it until a base HttpServletRequest is found.
     * 
     * @param request The request to unwrap.
     * @return The unwrapped request.
     */
    private static HttpServletRequest getUnwrapedRequest(HttpServletRequest request) {
        if (request instanceof HttpServletRequestWrapper) {
            final HttpServletRequestWrapper wrapper = (HttpServletRequestWrapper)request;
            final ServletRequest wrappedRequest = wrapper.getRequest();
            
            if (request != wrappedRequest && wrappedRequest instanceof HttpServletRequest)
                return getUnwrapedRequest((HttpServletRequest)wrappedRequest);
            else
                return wrapper;
        }
        else {
            return request;
        }
    }

    /**
     * Gets PortletWindowStateInfo for the PortletWindow, creates one if
     * nessesary.
     * 
     * @param window The PortletWindow to get the PortletWindowStateInfo for.
     * @return The PortletWindowStateInfo for the PortletWindow.
     */
    private static PortletWindowStateInfo getStateInfo(PortletWindow window) {
        final HttpSession session = getSession(window);
        final String stateKey = getKey(window);
        
        synchronized (session) {
            PortletWindowStateInfo stateInfo = (PortletWindowStateInfo)session.getAttribute(stateKey);
    
            if (stateInfo == null) {
                stateInfo = new PortletWindowStateInfo();
                session.setAttribute(stateKey, stateInfo);
            }
    
            return stateInfo;
        }
    }

    /**
     * Generates a key from the PortletWindow id.
     * 
     * @param window The PortletWindow to generate the key from.
     * @return The key for the PortletWindow.
     */
    private static String getKey(PortletWindow window) {
        final StringBuffer keyBuf = new StringBuffer();

        keyBuf.append(PortletWindowStateInfo.class.getName());
        keyBuf.append("_");
        keyBuf.append(window.getId());

        return keyBuf.toString();
    }
}
