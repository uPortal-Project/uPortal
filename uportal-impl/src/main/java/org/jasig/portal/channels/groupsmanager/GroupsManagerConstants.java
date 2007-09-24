/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.channels.groupsmanager;


/**
 * An interface that holds global variables uses to externalize settings from
 * the classes that use them.
 * @author Don Fracapane
 * @version $Revision$
 */
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
