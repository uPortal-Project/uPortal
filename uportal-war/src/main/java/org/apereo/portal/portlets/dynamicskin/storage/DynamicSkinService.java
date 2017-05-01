/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apereo.portal.portlets.dynamicskin.storage;

import java.util.SortedSet;

import javax.portlet.PortletRequest;

import org.apereo.portal.portlets.dynamicskin.DynamicSkinInstanceData;

/**
 * Services for the DynamicRespondrSkin portlet.
 *
 * @since 4.1.0
 */
public interface DynamicSkinService {

    /**
     * Returns path to skin CSS file for the provided data.  Path returned may be relative or absolute depending on the
     * implementation.
     * @param data skin instance data
     * @return path to skin CSS file
     */
    String getSkinCssPath(DynamicSkinInstanceData data);

    /**
     * Returns true if the skin CSS file already exists, or false otherwise.
     *
     * @param data skin instance data
     * @return true if CSS file exists; false otherwise
     */
    boolean skinCssFileExists(DynamicSkinInstanceData data);

    /**
     * Generates the skin CSS file for the provided skin instance data.  Once the CSS file is created, the path to the 
     * file can be accessed with the {@link #getSkinCssPath(DynamicSkinInstanceData)} method.
     * 
     * @param data skin instance data
     */
    void generateSkinCssFile(DynamicSkinInstanceData data);

    /**
     * Return set of skins that exist.
     *
     * @param request Portlet request
     * @return {@code SortedSet} of skin names. Set will be empty if there are errors.
     * @since 4.3
     */
    SortedSet<String> getSkinNames(PortletRequest request);

}
