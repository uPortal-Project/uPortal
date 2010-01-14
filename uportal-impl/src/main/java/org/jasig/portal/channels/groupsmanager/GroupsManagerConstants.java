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

package  org.jasig.portal.channels.groupsmanager;


/**
 * An interface that holds global variables uses to externalize settings from
 * the classes that use them.
 * @author Don Fracapane
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public interface GroupsManagerConstants {
   // some of these constants might be in the properties files
   String BASE_PACKAGE = "org.jasig.portal.channels.groupsmanager";
   String COMMANDS_PACKAGE = BASE_PACKAGE + ".commands";
   String WRAPPERS_PACKAGE = BASE_PACKAGE + ".wrappers";
   String GROUPS_PACKAGE = "org.jasig.portal.groups";
   String SECURITY_PACKAGE = "org.jasig.portal.security";
   String GROUP_CLASSNAME = GROUPS_PACKAGE + ".IEntityGroup";
   String ENTITY_CLASSNAME = SECURITY_PACKAGE + ".IPerson";
   String OWNER = "org.jasig.portal.channels.groupsmanager.CGroupsManager";
   String ENTITY_TAGNAME = "entity";
   String GROUP_TAGNAME = "group";
   String PROPERTIES_TAGNAME = "properties";

   // ROOT_GROUP_TITLE is the name of the root element holding the initial group
   // contexts in the groups management channel
   String ROOT_GROUP_TITLE = "Root Groups";
   String ROOT_GROUP_DESCRIPTION = "Set of Entity Types that have a root group.";

   String BROWSE_MODE = "browse";
   String EDIT_MODE = "edit";
   String SELECT_MODE = "select";
   String MEMBERS_ONLY_MODE = "members";

   String VIEW_PERMISSION = "VIEW";
   String CREATE_PERMISSION = "CREATE";
   String UPDATE_PERMISSION = "UPDATE";
   String DELETE_PERMISSION = "DELETE";
   String SELECT_PERMISSION = "SELECT";
   String ADD_REMOVE_PERMISSION="ADD/REMOVE";
   String ASSIGN_PERMISSION = "ASSIGNPERMISSIONS";
}
