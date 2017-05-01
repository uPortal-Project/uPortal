/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.layout.om;

import java.util.Collection;
import java.util.Map;
import javax.xml.transform.Transformer;
import org.apereo.portal.utils.Populator;

/**
 * Tracks customizations made by the users to the XSL transformer and layout attributes.
 * Implementations and returned data structures MUST be thread-safe
 *
 */
public interface IStylesheetUserPreferences {
    /** Unique identifier of these preferences */
    long getId();

    /**
     * Get an output property
     *
     * @param name output property name, cannot be null
     * @see Map#get(Object)
     */
    String getOutputProperty(String name);

    /**
     * Remove an output property
     *
     * @param name output property name, cannot be null
     * @see Map#remove(Object)
     */
    String removeOutputProperty(String name);

    /**
     * Get a stylesheet parameter
     *
     * @param name xslt parameter name, cannot be null
     * @see Map#get(Object)
     */
    String getStylesheetParameter(String name);

    /**
     * Set a transformer parameter
     *
     * @param name xslt parameter name, cannot be null
     * @param name xslt parameter value, cannot be null
     * @see Transformer#setParameter(String, Object)
     * @see Map#put(Object, Object)
     */
    String setStylesheetParameter(String name, String value);

    /**
     * Remove a transformer parameter
     *
     * @param name xslt parameter name, cannot be null
     * @see Map#remove(Object)
     */
    String removeStylesheetParameter(String name);

    /** Add all stylesheet parameters to the provided Map. IMPORTANT! Used in exporting profiles. */
    <P extends Populator<String, String>> P populateStylesheetParameters(P stylesheetParameters);

    /**
     * Get a layout attribute
     *
     * @param nodeId The layout node id to get the attribute for, cannot be null
     * @param name node attribute name, cannot be null
     * @see Map#get(Object)
     */
    String getLayoutAttribute(String nodeId, String name);

    /**
     * Set an attribute to add to a layout folder
     *
     * @param nodeId The layout node id to apply the attribute to, cannot be null
     * @param name node attribute name, cannot be null
     * @param value node attribute name, cannot be null
     * @see Map#put(Object, Object)
     */
    String setLayoutAttribute(String nodeId, String name, String value);

    /**
     * Get all nodes and values that have the specified attribute set.
     *
     * @param name The name of the layout attribute
     * @return Map of layout node id to attribute value
     */
    Map<String, String> getAllNodesAndValuesForAttribute(String name);

    /**
     * @param nodeId The layout node id to remove the attribute from, cannot be null
     * @param name node attribute name, cannot be null
     * @see Map#remove(Object)
     */
    String removeLayoutAttribute(String nodeId, String name);

    /**
     * Add all layout attributes for the specified nodeId to the provided Map
     *
     * @param nodeId The layout node id to get attributes for, cannot be null
     */
    <P extends Populator<String, String>> P populateLayoutAttributes(
            String nodeId, P layoutAttributes);

    /** @return Read-only view of all layout nodeIds stored in these preferences */
    Collection<String> getAllLayoutAttributeNodeIds();
}
