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

import javax.portlet.PortletPreferences;

/**
 * Interface for classes used to generate a unique token for a dynamic skin using the {@link PortletPreferences}.
 * The purpose of the generated token is for it to be used to uniquely name the skin less and css files.
 */
public interface DynamicSkinUniqueTokenGenerator {

    /**
     * Return a String hashcode of the portlet preference values in a repeatable fashion by calculating them based
     * on sorted portlet preference names.  Though hashcode does not guarantee uniqueness, from a practical
     * perspective we'll have so few different values we can reasonably assume preference value
     * combinations will be unique.
     *
     * @param request Portlet request
     * @return unique token for current skin based on portlet preference configuration values
     */
    String generateToken(final PortletPreferences portletPreferences);

}
