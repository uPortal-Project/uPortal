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
package org.apereo.portal.layout;

import java.util.Map;
import javax.servlet.http.HttpServletRequest;
import org.apereo.portal.IUserProfile;
import org.apereo.portal.layout.om.IStylesheetDescriptor;
import org.apereo.portal.layout.om.IStylesheetUserPreferences;
import org.apereo.portal.utils.Populator;

/**
 * Provides access to stylesheet user preference data. This is how any runtime code that needs
 * access to theme or structure stylesheet user preferences. All of the details around default
 * values, scoping and persistence are taken care of here.
 *
 */
public interface IStylesheetUserPreferencesService {
    enum PreferencesScope {
        THEME {
            @Override
            public int getStylesheetId(IUserProfile userProfile) {
                return userProfile.getThemeStylesheetId();
            }

            @Override
            public IStylesheetUserPreferences getDistributedIStylesheetUserPreferences(
                    IUserLayout userLayout) {
                return userLayout.getDistributedThemeStylesheetUserPreferences();
            }
        },
        STRUCTURE {
            @Override
            public int getStylesheetId(IUserProfile userProfile) {
                return userProfile.getStructureStylesheetId();
            }

            @Override
            public IStylesheetUserPreferences getDistributedIStylesheetUserPreferences(
                    IUserLayout userLayout) {
                return userLayout.getDistributedStructureStylesheetUserPreferences();
            }
        };

        public abstract int getStylesheetId(IUserProfile userProfile);

        public abstract IStylesheetUserPreferences getDistributedIStylesheetUserPreferences(
                IUserLayout userLayout);
    }

    /**
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @return The stylesheet descriptor for the current request and scope
     */
    IStylesheetDescriptor getStylesheetDescriptor(
            HttpServletRequest request, PreferencesScope prefScope);

    /**
     * Remove an output property
     *
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @see IStylesheetUserPreferences#removeOutputProperty(String)
     */
    String removeOutputProperty(
            HttpServletRequest request, PreferencesScope prefScope, String name);

    /**
     * Add all output properties to the provided Properties object
     *
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @see IStylesheetUserPreferences#populateOutputProperties(Populator)
     */
    <P extends Populator<String, String>> P populateOutputProperties(
            HttpServletRequest request, PreferencesScope prefScope, P properties);

    /**
     * Get a stylesheet parameter
     *
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @see IStylesheetUserPreferences#getStylesheetParameter(String)
     */
    String getStylesheetParameter(
            HttpServletRequest request, PreferencesScope prefScope, String name);

    /**
     * Set a transformer parameter
     *
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @see IStylesheetUserPreferences#setStylesheetParameter(String, String)
     */
    String setStylesheetParameter(
            HttpServletRequest request, PreferencesScope prefScope, String name, String value);

    /**
     * Remove a transformer parameter
     *
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @see IStylesheetUserPreferences#removeStylesheetParameter(String)
     */
    String removeStylesheetParameter(
            HttpServletRequest request, PreferencesScope prefScope, String name);

    /**
     * Add all stylesheet parameters to the provided Map
     *
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @see IStylesheetUserPreferences#populateStylesheetParameters(Populator)
     */
    <P extends Populator<String, String>> P populateStylesheetParameters(
            HttpServletRequest request, PreferencesScope prefScope, P stylesheetParameters);

    /**
     * Get a layout attribute
     *
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @see IStylesheetUserPreferences#getLayoutAttribute(String, String)
     */
    String getLayoutAttribute(
            HttpServletRequest request, PreferencesScope prefScope, String nodeId, String name);

    /**
     * Set an attribute to add to a layout folder
     *
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @see IStylesheetUserPreferences#setLayoutAttribute(String, String, String)
     */
    String setLayoutAttribute(
            HttpServletRequest request,
            PreferencesScope prefScope,
            String nodeId,
            String name,
            String value);

    /**
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @see IStylesheetUserPreferences#removeLayoutAttribute(String, String)
     */
    String removeLayoutAttribute(
            HttpServletRequest request, PreferencesScope prefScope, String nodeId, String name);

    /**
     * @return An iterable of all layout nodeIds
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @see IStylesheetUserPreferences#getAllLayoutAttributeNodeIds()
     */
    Iterable<String> getAllLayoutAttributeNodeIds(
            HttpServletRequest request, PreferencesScope prefScope);

    /**
     * Get all layout node ids that have the specified attribute applied to them
     *
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @param name The name of the layout attribute
     * @return Map of layoutNodeId to attribute value for all nodes that have an attribute with the
     *     specified name.
     */
    Map<String, String> getAllNodesAndValuesForAttribute(
            HttpServletRequest request, PreferencesScope prefScope, String name);

    /**
     * Add all layout attributes for the specified nodeId to the provided Map
     *
     * @param request The current request
     * @param prefScope The stylesheet preferences scope
     * @see IStylesheetUserPreferences#populateLayoutAttributes(String, Populator)
     */
    <P extends Populator<String, String>> P populateLayoutAttributes(
            HttpServletRequest request,
            PreferencesScope prefScope,
            String nodeId,
            P layoutAttributes);

    void setStructureStylesheetOverride(HttpServletRequest request, String override);

    void setThemeStyleSheetOverride(HttpServletRequest request, String override);
}
