/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. Redistributions of any form whatsoever must retain the following
 *    acknowledgment:
 *    "This product includes software developed by the JA-SIG Collaborative
 *    (http://www.jasig.org/)."
 *
 * THIS SOFTWARE IS PROVIDED BY THE JA-SIG COLLABORATIVE "AS IS" AND ANY
 * EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED.  IN NO EVENT SHALL THE JA-SIG COLLABORATIVE OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
 * OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 */

package  org.jasig.portal.channels.groupsmanager;

/**
 * <p>Title: uPortal</p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: Columbia University</p>
 * @author Don Fracapane
 * @version 2.0
 */
import  java.util.*;
import  java.lang.*;

/**
 * An interface that holds global variables uses to externalize settings from
 * the classes that use them.
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
   //HashMap ENTITY_TYPES = GroupsManagerXML.getEntityTypes();

   // ROOT_ELEMENT_TAGNAME is the name of the root element holding the initial group
   // contexts in the groups management channel
   String ROOT_GROUP_TITLE = "My Groups";
   String ROOT_GROUP_DESCRIPTION = "Set of Initial Group Contexts for this user";

   /** @todo Have to externalize all message strings in order to be "international"
    *  These strings will eventually be held in a properties file so languages can
    *  be swapped without recompiling. */
   //String MSG_OBJECT_NOT_FOUND = "The object you have attempted to operate upon was not found";
}



