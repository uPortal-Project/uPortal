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
package org.apereo.portal.portlets.dynamicskin;

/**
 * Defines constants used with the Dynamic Respondr Skin Portlet.
 */
public final class DynamicRespondrSkinConstants {

    /**
     * String that is prepended to preferences that are configurable, and also are passed into the LESS file as
     * variables (minus the prefix).  This insures someone can add a non-skin preference value in later as long
     * as it doesn't have this prefix and the preference will not impact the skin.
     */
    static final String CONFIGURABLE_PREFIX = "PREF";

    /**
     * Name of default skin.
     */
    public static final String DEFAULT_SKIN_NAME = "defaultSkin";

    /**
     * Name of the preference that indicates the name of the skin to use.
     */
    public static final String PREF_SKIN_NAME = CONFIGURABLE_PREFIX + "dynamicSkinName";

    /**
     * Name of preference used to determine whether or not dynamic skins is enabled.
     */
    public static final String PREF_DYNAMIC = CONFIGURABLE_PREFIX + "dynamicSkinEnabled";

    /**
     * Default relative root folder for skin files.
     */
    public static final String DEFAULT_RELATIVE_ROOT_FOLDER = "/media/skins/respondr";

    /**
     * Model name for boolean indicating whether or not the user can access the skin config.
     */
    public static final String CAN_ACCESS_SKIN_CONFIG_MODEL_NAME = "canAccessSkinConfig";

    /**
     * Model attribute name for skin CSS URL.
     */
    public static final String SKIN_CSS_URL_MODEL_ATTRIBUTE_NAME = "skinCssUrl";

    private DynamicRespondrSkinConstants() {
        // prevent instantiation
    }

}
