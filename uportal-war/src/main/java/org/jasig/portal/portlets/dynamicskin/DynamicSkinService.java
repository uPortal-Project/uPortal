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
package org.jasig.portal.portlets.dynamicskin;

import java.util.SortedSet;

import javax.portlet.PortletRequest;

/**
 * Services for the DynamicRespondrSkin portlet.
 *
 * @since 4.1.0
 * @author James Wennmacher, jwennmacher@unicon.net
 */
public interface DynamicSkinService {

    /**
     * String that is prepended to preferences that are configurable, and also are passed into the LESS file as
     * variables (minus the prefix).  This insures someone can add a non-skin preference value in later as long
     * as it doesn't have this prefix and the preference will not impact the skin.
     */
    static final String CONFIGURABLE_PREFIX = "PREF";

    /**
     * Return true if the filePathname already exists on the file system.
     *
     * @param filePathname Fully-qualified file path name of the .css file
     * @return True if file exists on the file system.
     */
    boolean skinFileExists(String filePathname);

    /**
     * Create the skin css file in a thread-safe manner that allows multiple different skin files to be created
     * simultaneously to handle large tenant situations where all the custom CSS files were cleared away after a
     * uPortal deploy.
     *
     * Since the less compilation phase is fairly slow (several seconds) and intensive, this method will
     * allow multiple threads to process different less compilations at the same time but ensure the same
     * output file will not be created multiple times. Also this method will not let a bad LESS file cause repeated
     * LESS compilations and completely take down the portal.  The bad file will be blacklisted for a period
     * of time to limit performance impacts.
     *
     * @param request Portlet Request
     * @param filePathname Fully-qualified file path name of the .css file to create
     * @param skinToken Likely unique value to add to the skin css filename
     * @param lessfileBaseName base name of the less file; e.g. a value of foo will compile foo.less.
     */
    void generateSkinCssFile(PortletRequest request, String filePathname, String skinToken, String lessfileBaseName);

    /**
     * Return a String hashcode of the portlet preference values in a repeatable fashion by calculating them based
     * on sorted portlet preference names.  Though hashcode does not guarantee uniqueness, from a practical
     * perspective we'll have so few different values we can reasonably assume preference value
     * combinations will be unique.
     *
     * This calculation process must duplicate computeDefaultHashcode.
     *
     * @param request Portlet request
     * @return Hashcode of portlet preference configuration values.
     */
    String calculateTokenForCurrentSkin(PortletRequest request);

    /**
     * Return set of skins that exist.
     *
     * @param request Portlet request
     * @return {@code SortedSet} of skin names. Set will be empty if there are errors.
     * @since 4.3
     */
    SortedSet<String> getSkinNames(PortletRequest request);

}
