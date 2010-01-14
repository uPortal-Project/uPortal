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
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
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