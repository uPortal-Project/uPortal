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

import java.util.HashMap;
import java.util.Set;

/**
 * A class designed to decouple a request for an action to be performed from the
 * class responsible for the action.  Also centralizes authorization for commands.
 * @author Don Fracapane
 * @version $Revision$
 */
public class GroupsManagerCommandFactory
      implements GroupsManagerConstants {
   protected static HashMap bindings = new java.util.HashMap(16);
   protected static GroupsManagerCommandFactory _instance = null;

   /**
    * Binds a hasmap name to an instance of a command object
    */
   protected GroupsManagerCommandFactory () {
      try {
         bindings.put("Add", Class.forName(COMMANDS_PACKAGE + ".AddMembers").newInstance());
         bindings.put("Permissions", Class.forName(COMMANDS_PACKAGE + ".AssignPermissions").newInstance());
         bindings.put("Cancel", Class.forName(COMMANDS_PACKAGE + ".CancelSelection").newInstance());
         bindings.put("Collapse", Class.forName(COMMANDS_PACKAGE + ".CollapseGroup").newInstance());
         bindings.put("Create", Class.forName(COMMANDS_PACKAGE + ".CreateGroup").newInstance());
         bindings.put("Delete", Class.forName(COMMANDS_PACKAGE + ".DeleteGroup").newInstance());
         bindings.put("Done", Class.forName(COMMANDS_PACKAGE + ".DoneWithSelection").newInstance());
         bindings.put("Lock", Class.forName(COMMANDS_PACKAGE + ".EditGroup").newInstance());
         bindings.put("Expand", Class.forName(COMMANDS_PACKAGE + ".ExpandGroup").newInstance());
         bindings.put("Search", Class.forName(COMMANDS_PACKAGE + ".Search").newInstance());
         bindings.put("Highlight", Class.forName(COMMANDS_PACKAGE + ".HighlightGroup").newInstance());
         bindings.put("Remove", Class.forName(COMMANDS_PACKAGE + ".RemoveMember").newInstance());
         bindings.put("Deselect", Class.forName(COMMANDS_PACKAGE + ".SelectMembers").newInstance());
         bindings.put("Select", Class.forName(COMMANDS_PACKAGE + ".SelectMembers").newInstance());
         bindings.put("Unlock", Class.forName(COMMANDS_PACKAGE + ".UnlockGroup").newInstance());
         bindings.put("Update", Class.forName(COMMANDS_PACKAGE + ".UpdateGroup").newInstance());
         bindings.put("ShowProperties", Class.forName(COMMANDS_PACKAGE + ".ShowProperties").newInstance());
         bindings.put("HideProperties", Class.forName(COMMANDS_PACKAGE + ".HideProperties").newInstance());
      } catch (Exception e) {
         Utility.logMessage("ERROR", "GroupsManagerCommandFactory::GroupsManagerCommandFactory(): \n"
               + e);
      }
   }

   /**
    * Instantiates the singleton if not already instantiated and returns it.
    * @return GroupsManagerCommandFactory
    */
   public static synchronized GroupsManagerCommandFactory instance () {
      Utility.logMessage("DEBUG", "GroupsManagerCommandFactory::instance(): about to get instance");
      if (_instance == null) {
         Utility.logMessage("DEBUG", "GroupsManagerCommandFactory::instance(): about to create instance");
         _instance = new GroupsManagerCommandFactory();
      }
      Utility.logMessage("DEBUG", "GroupsManagerCommandFactory::instance(): about to return instance");
      return  _instance;
   }

   /**
    * Returns the instance of a command object bound to a name.
    * @param name
    * @return IGroupsManagerCommand
    */
   public static IGroupsManagerCommand get (String name) {
      String cmdsString = "";
      String[] keys = getKeys();
      for (int i = 0; i < keys.length; i++) {
         cmdsString = cmdsString + keys[i] + "|";
      }
      Utility.logMessage("DEBUG", "GroupsManagerCommandFactory::get(): Command keys = : "
            + cmdsString);
      Utility.logMessage("DEBUG", "GroupsManagerCommandFactory::get(): about to get command: "
            + name);
      return  (IGroupsManagerCommand)bindings.get(name);
   }

   /**
    * Returns the names of the bound command objects. Used for debugging.
    * @return String[]
    */
   public static String[] getKeys () {
      Set keyset = bindings.keySet();
      return  (String[])keyset.toArray(new String[0]);
   }
}
