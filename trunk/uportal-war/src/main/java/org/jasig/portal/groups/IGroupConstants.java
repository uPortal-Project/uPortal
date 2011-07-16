/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.groups;

import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.security.IPerson;

/**
 * Defines constants for Groups related classes
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public interface IGroupConstants {
    public final String EVERYONE = IPerson.class.getName();
    public final String PORTLET_CATEGORIES = IPortletDefinition.class.getName();
    public final String PORTAL_ADMINISTRATORS = EVERYONE + ".PortalAdministrators";
    
    //Search method constants
    public final int IS =1;
    public final int STARTS_WITH=2;
    public final int ENDS_WITH=3;
    public final int CONTAINS=4;

    // Composite group service:
    public final String NODE_SEPARATOR = ".";
}
