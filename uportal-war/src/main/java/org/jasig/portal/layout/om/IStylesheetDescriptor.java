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

package org.jasig.portal.layout.om;

import java.util.Collection;
import java.util.Map;

import javax.xml.transform.Transformer;

import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.url.IUrlNodeSyntaxHelper;
import org.jasig.portal.url.IUrlNodeSyntaxHelperRegistry;

/**
 * Describes a XSL Stylesheet used in the rendering pipeline
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IStylesheetDescriptor extends IPortalData {
    
    /**
     * @return Unique ID of the descriptor
     */
    public long getId();
    
    /**
     * Unique, human readable name of the stylesheet
     */
    public String getName();
    
    /**
     * Optional description of the stylesheet
     */
    public void setDescription(String description);
    /**
     * Optional description of the stylesheet
     */
    public String getDescription();
    
    /**
     * Resource string used to locate the XSLT Stylesheet
     * Must be a valid Spring Resource string
     * http://static.springsource.org/spring/docs/3.0.x/spring-framework-reference/html/resources.html#resources-resource-strings
     */
    public void setStylesheetResource(String stylesheetResource);
    /**
     * Resource string used to locate the XSLT Stylesheet
     */
    public String getStylesheetResource();
    
    /**
     * The name of the {@link IUrlNodeSyntaxHelper} as it is registered in the {@link IUrlNodeSyntaxHelperRegistry}, can 
     * be null if no {@link IUrlNodeSyntaxHelper} is associated with this stylesheet.
     */
    public void setUrlNodeSyntaxHelperName(String urlNodeSyntaxHelperName);
    /**
     * @return The name of the {@link IUrlNodeSyntaxHelper} as it is registered in the {@link IUrlNodeSyntaxHelperRegistry}, can 
     * be null if no {@link IUrlNodeSyntaxHelper} is associated with this stylesheet.
     */
    public String getUrlNodeSyntaxHelperName();

    /**
     * Output properties to use on the {@link Transformer} for the stylesheet this defines
     * The returned Map is read-only
     */
    public Collection<IOutputPropertyDescriptor> getOutputPropertyDescriptors();
    public void setOutputPropertyDescriptors(Collection<IOutputPropertyDescriptor> outputPropertyDescriptors);
    /**
     * @see Map#get(Object)
     */
    public IOutputPropertyDescriptor getOutputPropertyDescriptor(String name);
    /**
     * @see Map#put(Object, Object)
     */
    public IOutputPropertyDescriptor setOutputPropertyDescriptor(IOutputPropertyDescriptor outputPropertyDescriptor);
    /**
     * @see Map#remove(Object)
     */
    public IOutputPropertyDescriptor removeOutputPropertyDescriptor(String name);
    
    /**
     * Parameters to use on the {@link Transformer} for the stylesheet this defines
     * The returned Map is read-only 
     */
    public Collection<IStylesheetParameterDescriptor> getStylesheetParameterDescriptors();
    public void setStylesheetParameterDescriptors(Collection<IStylesheetParameterDescriptor> stylesheetParameterDescriptors);
    /**
     * @see Map#get(Object)
     */
    public IStylesheetParameterDescriptor getStylesheetParameterDescriptor(String name);
    /**
     * @see Map#put(Object, Object)
     */
    public IStylesheetParameterDescriptor setStylesheetParameterDescriptor(IStylesheetParameterDescriptor stylesheetParameterDescriptor);
    /**
     * @see Map#remove(Object)
     */
    public IStylesheetParameterDescriptor removeStylesheetParameterDescriptor(String name);
    
    /**
     * Attributes to be added to layout elements prior to transformation by the stylesheet this defines
     * The returned Map is read-only
     */
    public Collection<ILayoutAttributeDescriptor> getLayoutAttributeDescriptors();
    public void setLayoutAttributeDescriptors(Collection<ILayoutAttributeDescriptor> layoutAttributeDescriptors);
    /**
     * @see Map#get(Object)
     */
    public ILayoutAttributeDescriptor getLayoutAttributeDescriptor(String name);
    /**
     * @see Map#put(Object, Object)
     */
    public ILayoutAttributeDescriptor setLayoutAttributeDescriptor(ILayoutAttributeDescriptor layoutAttributeDescriptor);
    /**
     * @see Map#remove(Object)
     */
    public ILayoutAttributeDescriptor removeLayoutAttributeDescriptor(String name);
}
