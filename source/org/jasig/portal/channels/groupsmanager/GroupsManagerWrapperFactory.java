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
 * A class designed to decouple a request for the creation of xml for an object
 * from the class requestor class.
 * @author Don Fracapane
 * @version $Revision$
 */
public class GroupsManagerWrapperFactory
      implements GroupsManagerConstants {
   protected static HashMap bindings = new java.util.HashMap(2);
   protected static GroupsManagerWrapperFactory _instance = null;

   /**
    * Binds a hashmap name to an instance of a wrapper object
    */
   protected GroupsManagerWrapperFactory () {
      try {
         bindings.put(ENTITY_TAGNAME, Class.forName(WRAPPERS_PACKAGE + ".EntityWrapper").newInstance());
         bindings.put(GROUP_TAGNAME, Class.forName(WRAPPERS_PACKAGE + ".GroupWrapper").newInstance());
      } catch (Exception e) {
         Utility.logMessage("ERROR", "GroupsManagerWrapperFactory:GroupsManagerWrapperFactory() \n"
               + e);
      }
   }

   /**
    * Instantiates the singleton if not already instantiated and returns it.
    * @return GroupsManagerWrapperFactory
    */
   public static synchronized GroupsManagerWrapperFactory instance () {
      Utility.logMessage("DEBUG", "GroupsManagerWrapperFactory.instance(): about to get instance");
      if (_instance == null) {
         Utility.logMessage("DEBUG", "GroupsManagerWrapperFactory.instance(): about to create instance");
         _instance = new GroupsManagerWrapperFactory();
      }
      Utility.logMessage("DEBUG", "GroupsManagerWrapperFactory.instance(): about to return instance");
      return  _instance;
   }

   /**
    * Returns the instance of a wrapper object bound to a name.
    * @param name
    * @return IGroupsManagerWrapper
    */
   public static IGroupsManagerWrapper get (String name) {
      return  (IGroupsManagerWrapper)bindings.get(name);
   }

   /**
    * Returns the names of the bound wrapper objects. Used for debugging.
    * @return String[]
    */
   public static String[] getKeys () {
      Set keyset = bindings.keySet();
      return  (String[])keyset.toArray(new String[0]);
   }
}



