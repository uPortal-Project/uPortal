/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
