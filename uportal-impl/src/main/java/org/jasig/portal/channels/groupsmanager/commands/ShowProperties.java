/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.channels.groupsmanager.commands;

import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.IBasicEntity;
import org.jasig.portal.channels.groupsmanager.CGroupsManagerSessionData;
import org.jasig.portal.channels.groupsmanager.GroupsManagerXML;
import org.jasig.portal.services.EntityPropertyRegistry;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * A Groups Manager command to expose properties from the EntityPropertyRegistry
 * for any entity or group
 *
 * @author Alex Vigdor
 * @version $Revision$
 */

public class ShowProperties extends GroupsManagerCommand {

  public ShowProperties() {
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
    if (e != null){
      if(e.getElementsByTagName(PROPERTIES_TAGNAME) != null && e.getElementsByTagName(PROPERTIES_TAGNAME).getLength() > 0)
        return;
      Element props = model.createElement(PROPERTIES_TAGNAME);
      EntityIdentifier ei = null;
      try{
        ei = new EntityIdentifier(e.getAttribute("key"), (Class<IBasicEntity>)Class.forName(e.getAttribute("type")));
      }
      catch (ClassNotFoundException ce){
        throw new RuntimeException("Unable to instantiate class:  type "+e.getAttribute("type")+" unknown");
      }
      String[] names = EntityPropertyRegistry.getPropertyNames(ei);
      //System.out.println("Found "+names.length+" properties");
      for(int i=0; i<names.length;i++){
          Element prop = model.createElement("property");
          prop.setAttribute("name",names[i]);
          prop.setAttribute("value",EntityPropertyRegistry.getProperty(ei,names[i]));
          props.appendChild(prop);
      }
      e.appendChild(props);
    }
  }
}