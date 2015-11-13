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
package org.jasig.portal.io.xml.portlettype;

import org.jasig.portal.security.IPermission;

/**
 * Set of supported permissions that can be used in the <permissions></permissions>
 * element of a portlet definition.
 *
 * @author Josh Helmer, jhelmer.unicon.net
 * @since 4.2
 */
public enum ExternalPermissionDefinition {
    SUBSCRIBE(IPermission.PORTAL_SUBSCRIBE, IPermission.PORTLET_SUBSCRIBER_ACTIVITY, false),
    BROWSE(IPermission.PORTAL_SUBSCRIBE, IPermission.PORTLET_BROWSE_ACTIVITY, true);

    private final String system;
    private final String activity;
    private boolean exportForPortletDef;


    ExternalPermissionDefinition(final String system, final String activity, final boolean export) {
        this.system = system;
        this.activity = activity;
        this.exportForPortletDef = export;
    }


    public String getSystem() {
        return system;
    }


    public String getActivity() {
        return activity;
    }


    public boolean getExportForPortletDef() {
        return exportForPortletDef;
    }


    public String toString() {
        return system + "." + activity;
    }


    /**
     * Given a system and activity, attempt to lookup a matching ExternalPermissionDefinition.
     *
     * @param system the system to lookup
     * @param activity the activity to lookup
     * @return the matching permission if one can be found, otherwise null
     */
    public static ExternalPermissionDefinition find(String system, String activity) {
        for (ExternalPermissionDefinition perm : ExternalPermissionDefinition.values()) {
            if (perm.system.equalsIgnoreCase(system) && perm.activity.equalsIgnoreCase(activity)) {
                return perm;
            }
        }

        return null;
    }
}
