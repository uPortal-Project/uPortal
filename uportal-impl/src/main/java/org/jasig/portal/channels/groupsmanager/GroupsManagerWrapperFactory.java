/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package  org.jasig.portal.channels.groupsmanager;

import java.util.HashMap;
import java.util.Set;

/**
 * A class designed to decouple a request for the creation of xml for an object
 * from the class requestor class.
 * @author Don Fracapane
 * @version $Revision$
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class GroupsManagerWrapperFactory
      implements GroupsManagerConstants {
   protected static HashMap BINDINGS = new java.util.HashMap(2);
   protected static boolean INITIALIZED = false;

 /**
    * Lazily initialize the static variables.
    * Binds a hashmap name to an instance of a wrapper object
    */
   public static void init (){
      if (INITIALIZED){
         return;
      }
      try {
         BINDINGS.put(ENTITY_TAGNAME, Class.forName(WRAPPERS_PACKAGE + ".EntityWrapper").newInstance());
         BINDINGS.put(GROUP_TAGNAME, Class.forName(WRAPPERS_PACKAGE + ".GroupWrapper").newInstance());
      } catch (Exception e) {
         Utility.logMessage("ERROR", "GroupsManagerWrapperFactory:init() \n" + e, e);
      }
      INITIALIZED = true;
   }

    /**
    * Returns the instance of a wrapper object bound to a name.
    * @param name
    * @return IGroupsManagerWrapper
    */
   public static IGroupsManagerWrapper get (String name) {
      init();
      return  (IGroupsManagerWrapper)BINDINGS.get(name);
   }

   /**
    * Returns the names of the bound wrapper objects. Used for debugging.
    * @return String[]
    */
   public static String[] getKeys () {
      init();
      Set keyset = BINDINGS.keySet();
      return  (String[])keyset.toArray(new String[0]);
   }
}



