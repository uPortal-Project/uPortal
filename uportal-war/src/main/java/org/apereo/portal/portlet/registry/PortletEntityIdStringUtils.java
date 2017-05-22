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
package org.apereo.portal.portlet.registry;

import java.util.regex.Pattern;

/**
 * Utility class for defining methods to be used for dealing with portlet entity ID strings.
 *
 * <p>A portlet entity ID string consists of three parts separated by underscores. The format is:
 * <portlet-entity-id>_<layout-node-id>_<user-id>
 *
 * <p>Examples: 88_n149_52 71_u54_12
 *
 * <p>Note that if a portlet has a 'delegate' portlet, then the 'layout-node-id' will have the
 * format: dlg-<delegate-portlet-window-id-with-underscores-replaced-by-dashes>
 *
 * <p>Examples: 90_dlg-5-ctf1-18_18 // here the portlet window ID of the delegate portlet is:
 * 5_ctf1_18 75_dlg-10-ctf2-32.tw_32 // here the portlet entity ID of the delegate portlet is:
 * 10_ctf2_32.tw
 *
 * <p>Currently, the admin interface uses Spring Webflow with 'delegate' portlets to display portlet
 * configuration views. When the admin interface portlet content is fetched with an 'exclusive'
 * window state URL, the layout node ID includes the "tw" transient (stateless) window instance ID.
 * This behavior was observed with RespondrJS UI using 'exclusive' window state URLs.
 *
 * @see flows/edit-portlet/configMode.jsp
 * @see org.apereo.portal.portlet.delegation.*
 * @see https://issues.jasig.org/browse/UP-2563
 * @see PortletWindowIdStringUtils
 * @see PortletWindowRegistryImpl#STATELESS_PORTLET_WINDOW_ID
 */
public class PortletEntityIdStringUtils {

    private static final char ID_PART_SEPARATOR = '_';
    private static final Pattern ID_PART_SEPARATOR_PATTERN =
            Pattern.compile(Pattern.quote(String.valueOf(ID_PART_SEPARATOR)));

    private static final char DELEGATE_LAYOUT_NODE_ID_SEPARATOR = '-';
    private static final String DELEGATE_LAYOUT_NODE_ID_PREFIX =
            "dlg" + DELEGATE_LAYOUT_NODE_ID_SEPARATOR;

    public static String format(
            final String portletDefinitionId, final String layoutNodeId, final int userId) {
        return portletDefinitionId + ID_PART_SEPARATOR + layoutNodeId + ID_PART_SEPARATOR + userId;
    }

    public static String format(
            final String portletDefinitionId, final String layoutNodeId, final String userId) {
        return portletDefinitionId + ID_PART_SEPARATOR + layoutNodeId + ID_PART_SEPARATOR + userId;
    }

    public static String convertToDelegateLayoutNodeId(final String portletEntityIdString) {
        return DELEGATE_LAYOUT_NODE_ID_PREFIX
                + portletEntityIdString.replace(
                        ID_PART_SEPARATOR, DELEGATE_LAYOUT_NODE_ID_SEPARATOR);
    }

    public static boolean isDelegateLayoutNode(final String layoutNodeId) {
        return layoutNodeId.startsWith(DELEGATE_LAYOUT_NODE_ID_PREFIX);
    }

    public static boolean hasCorrectNumberOfParts(final String portletEntityIdString) {
        return parseParts(portletEntityIdString).length == 3;
    }

    public static String parsePortletDefinitionId(final String portletEntityIdString) {
        final String[] parts = parseParts(portletEntityIdString);
        return parts.length > 0 ? parts[0] : null;
    }

    public static String parseLayoutNodeId(final String portletEntityIdString) {
        final String[] parts = parseParts(portletEntityIdString);
        return parts.length > 1 ? parts[1] : null;
    }

    public static String parseUserIdAsString(final String portletEntityIdString) {
        final String[] parts = parseParts(portletEntityIdString);
        return parts.length > 2 ? parts[2] : null;
    }

    private static String[] parseParts(final String portletEntityIdString) {
        return ID_PART_SEPARATOR_PATTERN.split(portletEntityIdString);
    }
}
