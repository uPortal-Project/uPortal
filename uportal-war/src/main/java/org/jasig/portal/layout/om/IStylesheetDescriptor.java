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

import java.util.Set;

import javax.xml.transform.Transformer;

import org.jasig.portal.dao.usertype.FunctionalNameType;

/**
 * Describes a XSL Stylesheet used in the rendering pipeline
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IStylesheetDescriptor {
    
    /**
     * @return Unique ID of the descriptor
     */
    public long getId();
    
    /**
     * Unique, human readable name of the stylesheet
     * Must validate against {@link FunctionalNameType#VALID_FNAME_PATTERN}
     */
    public void setName(String name);
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
     * Set of output properties to use on the {@link Transformer} for the stylesheet this defines
     */
    public Set<IOutputPropertyDescriptor> getOutputProperties();
    /**
     * Set of output properties to use on the {@link Transformer} for the stylesheet this defines
     */
    public void setOutputProperties(Set<IOutputPropertyDescriptor> outputProperties);
    
    /**
     * Set of parameters to use on the {@link Transformer} for the stylesheet this defines 
     */
    public Set<IStylesheetParameterDescriptor> getStylesheetParameters();
    /**
     * Set of parameters to use on the {@link Transformer} for the stylesheet this defines
     */
    public void setStylesheetParameters(Set<IStylesheetParameterDescriptor> stylesheetParameters);
    
    
    /**
     * Set of attributes to be added to layout elements prior to transformation by the stylesheet
     * this defines
     */
    public Set<ILayoutAttributeDescriptor> getLayoutAttributes();
    /**
     * Set of attributes to be added to layout elements prior to transformation by the stylesheet
     * this defines
     */
    public void setLayoutAttributes(Set<ILayoutAttributeDescriptor> layoutAttributes);
}
