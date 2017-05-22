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
import org.apache.commons.lang3.ArrayUtils;

/**
 * Utility class for defining methods to be used for dealing with portlet window ID strings.
 *
 * <p>A portlet window ID string is a portlet entity ID string that may contain a period separator
 * and a portlet window instance ID. Formats: <portlet-entity-id>
 * <portlet-entity-id>.<portlet-window-instance-id>
 *
 * <p>Examples: 88_n149_52 71_u54_12.2 10_ctf2_32.tw // the 'ctf' and 'tw' here are specific to
 * transient portlets
 *
 * @see PortletEntityIdStringUtils
 * @see TransientUserLayoutManagerWrapper#SUBSCRIBE_PREFIX
 * @see PortletWindowRegistryImpl#STATELESS_PORTLET_WINDOW_ID
 */
public class PortletWindowIdStringUtils {

    private static final char ID_PART_SEPARATOR = '.';
    private static final Pattern ID_PART_SEPARATOR_PATTERN =
            Pattern.compile(Pattern.quote(String.valueOf(ID_PART_SEPARATOR)));

    public static String format(
            final String portletEntityId, final String portletWindowInstanceId) {
        return (portletWindowInstanceId == null)
                ? portletEntityId
                : portletEntityId + ID_PART_SEPARATOR + portletWindowInstanceId;
    }

    public static String convertToDelegateLayoutNodeId(final String portletWindowIdString) {
        final String portletEntityIdString = parsePortletEntityId(portletWindowIdString);
        final String portletWindowInstanceIdString =
                parsePortletWindowInstanceId(portletWindowIdString);
        final String converted =
                PortletEntityIdStringUtils.convertToDelegateLayoutNodeId(portletEntityIdString);
        return (portletWindowInstanceIdString == null)
                ? converted
                : converted + ID_PART_SEPARATOR + portletWindowInstanceIdString;
    }

    public static String parsePortletEntityId(final String portletWindowIdString) {
        return parseParts(portletWindowIdString)[0];
    }

    public static String parsePortletWindowInstanceId(final String portletWindowIdString) {
        final String[] parts = parseParts(portletWindowIdString);
        return parts.length > 1 ? parts[1] : null;
    }

    public static boolean hasPortletWindowInstanceId(final String portletWindowIdString) {
        return parseParts(portletWindowIdString).length > 1;
    }

    public static boolean hasCorrectNumberOfParts(final String portletWindowIdString) {
        int length = parseParts(portletWindowIdString).length;
        return length == 1 || length == 2;
    }

    /**
     * Parses parts of the portlet window ID string. For input "88_n149_52", should return: {
     * "88_n149_52" } For input "88_n149_52.tw", should return: { "88_n149_52", "tw" } For input
     * "88_n149_52.1.2", should return: { "88_n149_52", "1", "2" } // note that the input here is
     * invalid due to having too many parts For input "146_dlg-5-ctf1-18.tw_18", should return: {
     * "146_dlg-5-ctf1-18.tw_18" } For input "146_dlg-5-ctf1-18.tw_18.1", should return: {
     * "146_dlg-5-ctf1-18.tw_18", "1" }
     */
    private static String[] parseParts(final String portletWindowIdString) {
        // Note that we cannot simply use the ID_PART_SEPARATOR_PATTERN on 'portletWindowIdString' because it may have a
        // 'delegate' layout node id that itself has a portlet window instance id, and thus the period in the layout node ID part
        // will mess up the parsing.
        // Example:  "146_dlg-5-ctf1-18.tw_18.1" would parse into { "146_dlg-5-ctf1-18", "tw_18", "1"} instead of { "146_dlg-5-ctf1-18.tw_18", "1" }
        final UserIdAndOtherParts userIdAndOtherParts =
                parseUserIdAndOtherParts(portletWindowIdString);
        if (userIdAndOtherParts.portletWindowInstanceId == null) {
            return new String[] {portletWindowIdString};
        } else {
            final String portletDefinitionId =
                    PortletEntityIdStringUtils.parsePortletDefinitionId(portletWindowIdString);
            final String layoutNodeId =
                    PortletEntityIdStringUtils.parseLayoutNodeId(portletWindowIdString);
            final String portletEntityId =
                    PortletEntityIdStringUtils.format(
                            portletDefinitionId, layoutNodeId, userIdAndOtherParts.userId);
            return createPartsArray(
                    portletEntityId,
                    userIdAndOtherParts.portletWindowInstanceId,
                    userIdAndOtherParts.extraParts);
        }
    }

    private static String[] createPartsArray(
            final String portletEntityId,
            final String portletWindowInstanceId,
            final String[] extraParts) {
        final String[] results = new String[2 + ((extraParts == null) ? 0 : extraParts.length)];
        results[0] = portletEntityId;
        results[1] = portletWindowInstanceId;
        if (extraParts != null) {
            for (int i = 0; i < extraParts.length; i++) {
                results[i + 2] = extraParts[i];
            }
        }
        return results;
    }

    private static UserIdAndOtherParts parseUserIdAndOtherParts(
            final String portletWindowIdString) {
        final UserIdAndOtherParts result = new UserIdAndOtherParts();
        final String userId = PortletEntityIdStringUtils.parseUserIdAsString(portletWindowIdString);
        final String[] parts = ID_PART_SEPARATOR_PATTERN.split(userId);
        if (parts.length > 0) {
            result.userId = parts[0];
            if (parts.length > 1) {
                result.portletWindowInstanceId = parts[1];
                if (parts.length > 2) {
                    result.extraParts = ArrayUtils.subarray(parts, 2, parts.length);
                }
            }
        }
        return result;
    }

    static class UserIdAndOtherParts {
        public UserIdAndOtherParts() {}

        public String userId;
        public String portletWindowInstanceId;
        public String[] extraParts;

        public int getPartsCount() {
            return (userId == null)
                    ? 0
                    : (portletWindowInstanceId == null)
                            ? 1
                            : (extraParts == null) ? 2 : 2 + extraParts.length;
        }
    }
}
