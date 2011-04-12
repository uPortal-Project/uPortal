/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlet.delegation.jsp;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.portlet.PortletMode;
import javax.portlet.WindowState;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.tagext.TagSupport;

import org.jasig.portal.portlet.PortletUtils;

/**
 * Simple wrapper tag for setting up the basis of URLs for portlet delegation
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class ParentUrlTag extends TagSupport implements ParameterizableTag {
    private static final long serialVersionUID = 1L;

    private String windowState = null;
    private String portletMode = null;
    private Map<String, List<String>> parameters;
    
    public String getPortletMode() {
        return portletMode;
    }
    /**
     * @param portletMode The portletMode to set
     */
    public void setPortletMode(String portletMode) {
        this.portletMode = portletMode;
    }

    public String getWindowState() {
        return windowState;
    }
    /**
     * @param windowState The windowState to set
     */
    public void setWindowState(String windowState) {
        this.windowState = windowState;
    }


    @Override
    public int doStartTag() throws JspException {
        this.parameters = new LinkedHashMap<String, List<String>>();
        
        return EVAL_BODY_INCLUDE;
    }
    
    @Override
    public int doEndTag() throws JspException {
        final RenderPortletTag renderPortletTag = (RenderPortletTag)findAncestorWithClass(this, RenderPortletTag.class);
        
        if (this.windowState != null) {
            final WindowState state = PortletUtils.getWindowState(this.windowState);
            renderPortletTag.setParentUrlState(state);
        }
        
        if (this.portletMode != null) {
            final PortletMode mode = PortletUtils.getPortletMode(this.portletMode);
            renderPortletTag.setParentUrlMode(mode);
        }
        
        renderPortletTag.setParentUrlParameters(this.parameters);
        
        return EVAL_PAGE;
    }
    
    @Override
    public void addParameter(String name, String value) {
        List<String> values = this.parameters.get(name);
        if (values == null) {
            values = new LinkedList<String>();
            this.parameters.put(name, values);
        }
        values.add(value);
    }
}
