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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;

import org.apache.pluto.om.window.PortletWindow;
import org.apache.pluto.util.StringUtils;

/**
 * Implementation of Apache Pluto PortalControlParameter.
 * @author Michael Ivanov, mvi@immagic.com
 * @version $Revision$
 */
public class PortalControlParameter {

    static public final String ACTION = "action";
    static public final String MODE = "mode";
    static public final String PORTLET_ID = "pid";
    static public final String PREFIX = "uP_";
    static public final String PREV_MODE = "prevMode";
    static public final String PREV_STATE = "prevState";
    static public final String RENDER_PARAM = "renderParam";
    static public final String STATE = "state";

    private Map requestParameters = new HashMap();
    private Map statefulControlParameters = new HashMap();
    private Map statelessControlParameters = new HashMap();
    private String portalURL = null;

    public static String decodeParameterName(String paramName) {
        return paramName.substring(PREFIX.length());
    }

    public static String decodeParameterValue(String paramName, String paramValue) {
        return paramValue;
    }

    private static String decodeRenderParamName(String encodedParamName) {
        StringTokenizer tokenizer = new StringTokenizer(encodedParamName, "_");
        if (!tokenizer.hasMoreTokens())
            return null;
        String constant = tokenizer.nextToken();
        if (!tokenizer.hasMoreTokens())
            return null;
        String objectId = tokenizer.nextToken();
        if (!tokenizer.hasMoreTokens())
            return null;
        String name = tokenizer.nextToken();
        return name;
    }

    private static String[] decodeRenderParamValues(String encodedParamValues) {
        StringTokenizer tokenizer = new StringTokenizer(encodedParamValues, "_");
        if (!tokenizer.hasMoreTokens())
            return null;
        String _count = tokenizer.nextToken();
        int count = Integer.valueOf(_count).intValue();
        String[] values = new String[count];
        for (int i = 0; i < count; i++) {
            if (!tokenizer.hasMoreTokens())
                return null;
            values[i] = decodeValue(tokenizer.nextToken());
        }
        return values;
    }

    private static String decodeValue(String value) {
        value = StringUtils.replace(value, "0x1", "_");
        value = StringUtils.replace(value, "0x2", ".");
        value = StringUtils.replace(value, "0x3", "/");
        value = StringUtils.replace(value, "0x4", "\r");
        value = StringUtils.replace(value, "0x5", "\n");
        value = StringUtils.replace(value, "0x6", "<");
        value = StringUtils.replace(value, "0x7", ">");
        value = StringUtils.replace(value, "0x8", " ");
        return value;
    }

    public static String encodeParameter(String param) {
        return PREFIX + param;
    }

    public static String encodeRenderParamName(PortletWindow window, String paramName) {
        StringBuffer returnvalue = new StringBuffer(50);
        returnvalue.append(RENDER_PARAM);
        returnvalue.append("_");
        returnvalue.append(window.getId().toString());
        returnvalue.append("_");
        returnvalue.append(paramName);
        return returnvalue.toString();
    }

    public static String encodeRenderParamValues(String[] paramValues) {
        StringBuffer returnvalue = new StringBuffer(100);
        returnvalue.append(paramValues.length);
        for (int i = 0; i < paramValues.length; i++) {
            returnvalue.append("_");
            returnvalue.append(encodeValue(paramValues[i]));
        }
        return returnvalue.toString();
    }

    private static String encodeValue(String value) {
        value = StringUtils.replace(value, "_", "0x1");
        value = StringUtils.replace(value, ".", "0x2");
        value = StringUtils.replace(value, "/", "0x3");
        value = StringUtils.replace(value, "\r", "0x4");
        value = StringUtils.replace(value, "\n", "0x5");
        value = StringUtils.replace(value, "<", "0x6");
        value = StringUtils.replace(value, ">", "0x7");
        value = StringUtils.replace(value, " ", "0x8");
        return value;
    }

    public static String getRenderParamKey(PortletWindow window) {
        return RENDER_PARAM + "_" + window.getId().toString();
    }

    public static boolean isControlParameter(String param) {
        return param.startsWith(PREFIX);
    }

    public static boolean isstatefulParameter(String param) {
        if (isControlParameter(param)) {
            if ((param.startsWith(PREFIX + MODE))
                || (param.startsWith(PREFIX + PREV_MODE))
                || (param.startsWith(PREFIX + STATE))
                || (param.startsWith(PREFIX + PREV_STATE))
                || (param.startsWith(PREFIX + RENDER_PARAM))) {
                return true;
            }
        }
        return false;
    }

    public PortalControlParameter(String url) {
        this.portalURL = url;
        // GET statefulControlParameters and statelessControlParameters from portalURL!!!
	
    }

    public void clearRenderParameters(PortletWindow portletWindow) {
        String prefix = getRenderParamKey(portletWindow);
        Iterator keyIterator = statefulControlParameters.keySet().iterator();

        while (keyIterator.hasNext()) {
            String name = (String)keyIterator.next();
            if (name.startsWith(prefix)) {
                keyIterator.remove();
            }
        }
    }

    private String getActionKey(PortletWindow window) {
        return ACTION + "_" + window.getId().toString();
    }

    public String[] getActionParameter(PortletWindow window, String paramName) {
        String encodedValues = (String)statefulControlParameters.get(encodeRenderParamName(window, paramName));
        String[] values = decodeRenderParamValues(encodedValues);
        return values;
    }

    public PortletMode getMode(PortletWindow window) {
        String mode = (String)statefulControlParameters.get(getModeKey(window));
        if (mode != null)
            return new PortletMode(mode);
        else
            return PortletMode.VIEW;
    }

    private String getModeKey(PortletWindow window) {
        return MODE + "_" + window.getId().toString();
    }

    public String getPIDValue() {
        String value = (String)statelessControlParameters.get(getPortletIdKey());
        return value == null ? "" : value;
    }

    private String getPortletIdKey() {
        return PORTLET_ID;
    }

    public PortletWindow getPortletWindowOfAction() {
        Iterator iterator = getStatelessControlParameters().keySet().iterator();
        while (iterator.hasNext()) {
            String name = (String)iterator.next();
            if (name.startsWith(ACTION)) {
                String id = name.substring(ACTION.length() + 1);
                /*Fragment fragment = org.apache.pluto.portalImpl.services.pageregistry.PageRegistry.getFragment(id);
                if (fragment instanceof PortletFragment) {
                    return((PortletFragment)fragment).getPortletWindow();
                }*/
                // TO IMPLEMENT
            }
        }
        return null;
    }

    public PortletMode getPrevMode(PortletWindow window) {
        String mode = (String)statefulControlParameters.get(getPrevModeKey(window));
        if (mode != null)
            return new PortletMode(mode);
        else
            return PortletMode.VIEW;
    }
    private String getPrevModeKey(PortletWindow window) {
        return PREV_MODE + "_" + window.getId().toString();
    }

    public WindowState getPrevState(PortletWindow window) {
        String state = (String)statefulControlParameters.get(getPrevStateKey(window));
        if (state != null)
            return new WindowState(state);
        else
            return WindowState.NORMAL;
    }
    private String getPrevStateKey(PortletWindow window) {
        return PREV_STATE + "_" + window.getId().toString();
    }

    public Iterator getRenderParamNames(PortletWindow window) {
        ArrayList returnvalue = new ArrayList();
        String prefix = getRenderParamKey(window);
        Iterator keyIterator = statefulControlParameters.keySet().iterator();

        while (keyIterator.hasNext()) {
            String name = (String)keyIterator.next();
            if (name.startsWith(prefix)) {
                returnvalue.add(name.substring(prefix.length() + 1));
            }
        }

        return returnvalue.iterator();
    }

    public String[] getRenderParamValues(PortletWindow window, String paramName) {
        String encodedValues = (String)statefulControlParameters.get(encodeRenderParamName(window, paramName));
        String[] values = decodeRenderParamValues(encodedValues);
        return values;
    }

    public Map getRequestParameters() {
        return requestParameters;
    }

    public WindowState getState(PortletWindow window) {
        String state = (String)statefulControlParameters.get(getStateKey(window));
        if (state != null)
            return new WindowState(state);
        else
            return WindowState.NORMAL;
    }

    public Map getstatefulControlParameters() {
        return statefulControlParameters;
    }

    private String getStateKey(PortletWindow window) {
        return STATE + "_" + window.getId().toString();
    }

    public Map getStatelessControlParameters() {
        return statelessControlParameters;
    }

    public boolean isOnePortletWindowMaximized() {
        Iterator iterator = statefulControlParameters.keySet().iterator();
        while (iterator.hasNext()) {
            String name = (String)iterator.next();
            if (name.startsWith(STATE)) {
                if (statefulControlParameters.get(name).equals(WindowState.MAXIMIZED.toString())) {
                    return true;
                }
            }
        }
        return false;
    }

    public void setAction(PortletWindow window) {
        getstatefulControlParameters().put(getActionKey(window), ACTION.toUpperCase() );
    }

    public void setMode(PortletWindow window, PortletMode mode) {
        Object prevMode = statefulControlParameters.get(getModeKey(window));
        if (prevMode != null)
            statefulControlParameters.put(getPrevModeKey(window), prevMode);
        // set current mode
        statefulControlParameters.put(getModeKey(window), mode.toString());
    }

    public void setPortletId(PortletWindow window) {
        getstatefulControlParameters().put(getPortletIdKey(), window.getId().toString());
        //getStateLessControlParameter().put(getPortletIdKey(),window.getId().toString());
    }

    public void setRenderParam(PortletWindow window, String name, String[] values) {
        statefulControlParameters.put(encodeRenderParamName(window, name), encodeRenderParamValues(values));
    }

    public void setRequestParam(String name, String[] values) {
        requestParameters.put(name, values);
    }

    public void setState(PortletWindow window, WindowState state) {
        Object prevState = statefulControlParameters.get(getStateKey(window));
        if (prevState != null)
            statefulControlParameters.put(getPrevStateKey(window), prevState);
        statefulControlParameters.put(getStateKey(window), state.toString());
    }

}
