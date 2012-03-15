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
import java.util.Properties;

import javax.xml.transform.Transformer;

/**
 * Tracks customizations made by the users to the XSL transformer and layout attributes.
 * Implementations and returned data structures MUST be thread-safe
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface IStylesheetUserPreferences {
    /**
     * Unique identifier of these preferences
     */
    public long getId();

    public long getStylesheetDescriptorId();

    public int getUserId();

    public int getProfileId();
    
    /**
     * Get an output property
     * @see Map#get(Object)
     */
    public String getOutputProperty(String name);
    
    /**
     * Set an output property
     * @see Transformer#setOutputProperty(String, String)
     * @see Map#put(Object, Object)
     */
    public String setOutputProperty(String name, String value);
    
    /**
     * Remove an output property
     * @see Map#remove(Object)
     */
    public String removeOutputProperty(String name);
    
    /**
     * Add all output properties to the provided Properties object
     */
    public Properties populateOutputProperties(Properties properties);
    
    /**
     * @see Properties#clear();
     */
    public void clearOutputProperties();
    

    /**
     * Get a stylesheet parameter
     * @see Map#get(Object)
     */
    public String getStylesheetParameter(String name);
    
    /**
     * Set a transformer parameter
     * @see Transformer#setParameter(String, Object)
     * @see Map#put(Object, Object)
     */
    public String setStylesheetParameter(String name, String value);
    
    /**
     * Remove a transformer parameter
     * @see Map#remove(Object)
     */
    public String removeStylesheetParameter(String name);
    
    /**
     * Add all stylesheet parameters to the provided Map
     */
    public Map<String, String> populateStylesheetParameters(Map<String, String> stylesheetParameters);
    
    /**
     * @see Map#clear();
     */
    public void clearStylesheetParameters();

    
    /**
     * Get a layout attribute
     * @see Map#get(Object)
     */
    public String getLayoutAttribute(String nodeId, String name);
    
    /**
     * Set an attribute to add to a layout folder
     *  
     * @param nodeId The layout node id to apply the attribute to
     * @see Map#put(Object, Object)
     */
    public String setLayoutAttribute(String nodeId, String name, String value);
    
    /**
     * @param nodeId The layout node id to remove the attribute from
     * @see Map#remove(Object)
     */
    public String removeLayoutAttribute(String nodeId, String name);
    
    /**
     * Add all layout attributes for the specified nodeId to the provided Map
     */
    public Map<String, String> populateLayoutAttributes(String nodeId, Map<String, String> layoutAttributes);
    
    /**
     * Add all layout attributes for all nodeIds to the provided Map
     */
    public Map<String, Map<String, String>> populateAllLayoutAttributes(Map<String, Map<String, String>> allLayoutAttributes);
  
    
    /**
     * @return Read-only view of all layout nodeIds stored in these preferences
     */
    public Collection<String> getAllLayoutAttributeNodeIds();
    
    /**
     * @see Map#clear();
     */
    public void clearLayoutAttributes(String nodeId);

    
    /**
     * @see Map#clear();
     */
    public void clearAllLayoutAttributes();

}
