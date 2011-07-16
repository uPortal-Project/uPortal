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

package org.jasig.portal.url;

import javax.servlet.http.HttpServletRequest;

import org.jasig.portal.layout.om.IStylesheetDescriptor;

/**
 * A registry of all available {@link IUrlNodeSyntaxHelper} impls
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IUrlNodeSyntaxHelperRegistry {
    /**
     * Get the registered {@link IUrlNodeSyntaxHelper} 
     */
    public IUrlNodeSyntaxHelper getUrlNodeSyntaxHelper(String name);
    
    /**
     * Get the {@link IUrlNodeSyntaxHelper} specified by the {@link IStylesheetDescriptor}, returns null if
     * {@link IStylesheetDescriptor#getUrlNodeSyntaxHelperName()} returns null.
     * @throws IllegalArgumentException if no stylesheet descriptor is found for the id
     */
    public IUrlNodeSyntaxHelper getUrlNodeSyntaxHelperForStylesheet(int stylesheetDescriptorId);
    
    /**
     * Get the {@link IUrlNodeSyntaxHelper} for the current theme or structure stylesheet descriptor. Never returns null.
     * @throws IllegalArgumentException if no stylesheet descriptor is found for the theme or structure descriptor
     * @throws IllegalStateException if no IUrlNodeSyntaxHelper is found for the current stylesheet descriptor(s)
     */
    public IUrlNodeSyntaxHelper getCurrentUrlNodeSyntaxHelper(HttpServletRequest request);
}
