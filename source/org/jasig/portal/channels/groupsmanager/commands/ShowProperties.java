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

package org.jasig.portal.channels.groupsmanager.commands;

import org.jasig.portal.EntityIdentifier;
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
    * @throws Exception
    * @param sessionData
    */
  public void execute (CGroupsManagerSessionData sessionData) throws Exception{
    Document model = getXmlDoc(sessionData);
    String id = this.getCommandArg(sessionData.runtimeData);
    Element e = GroupsManagerXML.getElementById(model,id);
    if (e != null){
      Element props = model.createElement("properties");
      EntityIdentifier ei = null;
      try{
        ei = new EntityIdentifier(e.getAttribute("key"),Class.forName(e.getAttribute("type")));
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