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
   protected static HashMap BINDINGS = new java.util.HashMap(16);
   protected static boolean INITIALIZED = false;

 /**
    * Lazily initialize the static variables.
    * Binds a hasmap name to an instance of a command object
    */
   public static void init (){
      if (INITIALIZED){
         return;
      }
      try {
         BINDINGS.put("Add", Class.forName(COMMANDS_PACKAGE + ".AddMembers").newInstance());
         BINDINGS.put("Permissions", Class.forName(COMMANDS_PACKAGE + ".AssignPermissions").newInstance());
         BINDINGS.put("Cancel", Class.forName(COMMANDS_PACKAGE + ".CancelSelection").newInstance());
         BINDINGS.put("Collapse", Class.forName(COMMANDS_PACKAGE + ".CollapseGroup").newInstance());
         BINDINGS.put("Create", Class.forName(COMMANDS_PACKAGE + ".CreateGroup").newInstance());
         BINDINGS.put("Delete", Class.forName(COMMANDS_PACKAGE + ".DeleteGroup").newInstance());
         BINDINGS.put("Done", Class.forName(COMMANDS_PACKAGE + ".DoneWithSelection").newInstance());
         BINDINGS.put("Lock", Class.forName(COMMANDS_PACKAGE + ".EditGroup").newInstance());
         BINDINGS.put("Expand", Class.forName(COMMANDS_PACKAGE + ".ExpandGroup").newInstance());
         BINDINGS.put("Search", Class.forName(COMMANDS_PACKAGE + ".Search").newInstance());
         BINDINGS.put("Highlight", Class.forName(COMMANDS_PACKAGE + ".HighlightGroup").newInstance());
         BINDINGS.put("Remove", Class.forName(COMMANDS_PACKAGE + ".RemoveMember").newInstance());
         BINDINGS.put("Deselect", Class.forName(COMMANDS_PACKAGE + ".SelectMembers").newInstance());
         BINDINGS.put("Select", Class.forName(COMMANDS_PACKAGE + ".SelectMembers").newInstance());
         BINDINGS.put("Unlock", Class.forName(COMMANDS_PACKAGE + ".UnlockGroup").newInstance());
         BINDINGS.put("Update", Class.forName(COMMANDS_PACKAGE + ".UpdateGroup").newInstance());
         BINDINGS.put("ShowProperties", Class.forName(COMMANDS_PACKAGE + ".ShowProperties").newInstance());
         BINDINGS.put("HideProperties", Class.forName(COMMANDS_PACKAGE + ".HideProperties").newInstance());
      } catch (Exception e) {
         Utility.logMessage("ERROR", "GroupsManagerCommandFactory::init(): \n" + e, e);
      }
      INITIALIZED = true;
   }

   /**
    * Returns the instance of a command object bound to a name.
    * @param name
    * @return IGroupsManagerCommand
    */
   public static IGroupsManagerCommand get (String name) {
      init();
      String cmdsString = "";
      String[] keys = getKeys();
      for (int i = 0; i < keys.length; i++) {
         cmdsString = cmdsString + keys[i] + "|";
      }
      Utility.logMessage("DEBUG", "GroupsManagerCommandFactory::get(): Command keys = : "
            + cmdsString);
      Utility.logMessage("DEBUG", "GroupsManagerCommandFactory::get(): about to get command: "
            + name);
      return  (IGroupsManagerCommand)BINDINGS.get(name);
   }

   /**
    * Returns the names of the bound command objects. Used for debugging.
    * @return String[]
    */
   public static String[] getKeys () {
      init();
      Set keyset = BINDINGS.keySet();
      return  (String[])keyset.toArray(new String[0]);
   }
}
