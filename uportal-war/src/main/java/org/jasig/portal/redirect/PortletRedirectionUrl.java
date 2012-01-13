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
package org.jasig.portal.redirect;

import javax.portlet.PortletMode;

import org.jasig.portal.url.UrlType;

/**
 * PortletRedirectionUrl represents a redirection service target that is a
 * portlet inside this portal container.
 * 
 * @author Jen Bourey, jennifer.bourey@gmail.com
 */
public class PortletRedirectionUrl extends AbstractRedirectionUrl {

    private String fname;
    private UrlType type;
    private PortletMode mode;

    /**
     * Get the fname of the portlet to be used in the target url.
     * 
     * @return
     */
    public String getFname() {
        return fname;
    }

    /**
     * Set the fname of the portlet to be used in th target url.
     * 
     * @param fname
     */
    public void setFname(String fname) {
        this.fname = fname;
    }

    /**
     * Get the target url type (render, action, etc.).
     * 
     * @return
     */
    public UrlType getType() {
        return type;
    }

    /**
     * Set the target url type (render, action, etc.)
     * 
     * @param type
     */
    public void setType(UrlType type) {
        this.type = type;
    }

    /**
     * Get the target url portlet mode.
     * 
     * @return
     */
    public PortletMode getMode() {
        return mode;
    }

    /**
     * Set the target url portlet mode.
     * 
     * @param mode
     */
    public void setMode(PortletMode mode) {
        this.mode = mode;
    }

}
