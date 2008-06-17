/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.groupsmanager.commands;

import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
/**
 * A Groups Manager command to hide properties for any entity or group
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public class HideProperties extends GroupsManagerCommand {

   public HideProperties() {
   }

   /**
    * put your documentation comment here
    * @param sessionData
    * @throws Exception
    */
   public void execute (CGroupsManagerSessionData sessionData) throws Exception{
      Document model = getXmlDoc(sessionData);
      String id = this.getCommandArg(sessionData.runtimeData);
      Element e = GroupsManagerXML.getElementById(model,id);
      GroupsManagerXML.removeElementsForTagName(e, PROPERTIES_TAGNAME);
   }
}