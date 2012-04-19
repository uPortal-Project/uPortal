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
package org.jasig.portal.portlet.rendering;

import java.util.Locale;

import javax.portlet.ResourceResponse;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

/**
 * Used to handle output from portlet resource requests.
 * 
 * @author Eric Dalquist
 */
public interface PortletResourceOutputHandler extends PortletOutputHandler {
    
    /**
     * @see ResourceResponse#setCharacterEncoding(String)
     * @see ServletResponse#setCharacterEncoding(String)
     */
    void setCharacterEncoding(String charset);
    
    /**
     * @see ResourceResponse#setContentLength(int)
     * @see ServletResponse#setContentLength(int)
     */
    void setContentLength(int len);
    
    /**
     * @see ResourceResponse#setLocale(Locale)
     * @see ServletResponse#setLocale(Locale)
     */
    void setLocale(Locale locale);
    
    /**
     * @see HttpServletResponse#setStatus(int)
     */
    void setStatus(int status);
    
    /**
     * @see HttpServletResponse#setDateHeader(String, long)
     */
    void setDateHeader(String name, long date);
    
    /**
     * @see HttpServletResponse#addDateHeader(String, long)
     */
    void addDateHeader(String name, long date);
    
    /**
     * @see HttpServletResponse#setHeader(String, String)
     */
    void setHeader(String name, String value);
    
    /**
     * @see HttpServletResponse#addHeader(String, String)
     */
    void addHeader(String name, String value);

    /**
     * @see HttpServletResponse#setIntHeader(String, int)
     */
    void setIntHeader(String name, int value);

    /**
     * @see HttpServletResponse#addIntHeader(String, int)
     */
    void addIntHeader(String name, int value);
}
