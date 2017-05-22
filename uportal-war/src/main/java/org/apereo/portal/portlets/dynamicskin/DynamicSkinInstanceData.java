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

import java.util.Map;

import javax.portlet.PortletRequest;

/**
 * Interface for classes used to store data about a dynamic skin instance.
 */
public interface DynamicSkinInstanceData {

    /**
     * Returns name of skin to use for current portlet request.
     * @return skin name
     */
    String getSkinName();
    /**
     * Returns the absolute path root for the portlet.
     * @return portlet absolute path root
     */
    String getPortletAbsolutePathRoot();
    /**
     * Returns the {@link PortletRequest} object for the current request.
     * @return the portlet request object
     */
    PortletRequest getPortletRequest();
    /**
     * Returns the map of variables that will be used in the LESS compilation for the dynamic skin.
     * @return variables map to be used for LESS compilation
     */
    Map<String, String> getVariableNameToValueMap();

}
