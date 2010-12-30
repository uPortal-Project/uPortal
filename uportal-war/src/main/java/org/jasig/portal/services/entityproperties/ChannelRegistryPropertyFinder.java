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

package  org.jasig.portal.services.entityproperties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.spring.locator.PortletDefinitionRegistryLocator;


/**
 * A finder implementation to provide channel properties derived from the
 * ChannelRegistryManager
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public class ChannelRegistryPropertyFinder
        implements IEntityPropertyFinder {
    
    private static final Log log = LogFactory.getLog(ChannelRegistryPropertyFinder.class);
    
   protected static String[] names;
   protected static final Class chan = IPortletDefinition.class;
   protected static boolean INITIALIZED = false;

 /**
    * Lazily initialize the static variables.
    */
   public static void init (){
      if (INITIALIZED){
         return;
      }
      /* this array should hold the desired attributes of a channel element
      as defined in channelRegistry.dtd
      */
      names = new String[5];
      names[0] = "Name";
      names[1] = "Title";
      names[2] = "Description";
      names[3] = "Functional Name";
      names[4] = "Timeout";
      INITIALIZED = true;
   }

    public ChannelRegistryPropertyFinder() {
       init();
    }

    public String[] getPropertyNames(EntityIdentifier entityID) {
        return  names;
    }

    public String getProperty(EntityIdentifier entityID, String name) {
        String r = null;
        if (entityID.getType().equals(chan)) {
            try {
                //Element c = ChannelRegistryManager.getChannel("chan"+entityID.getKey());
                //r = c.getAttribute(name);
            	IPortletDefinition cd = PortletDefinitionRegistryLocator.getPortletDefinitionRegistry().getPortletDefinition(entityID.getKey());
                if (name.equals("Name")){
                  r = cd.getName(); 
                }
                if (name.equals("Title")){
                  r = cd.getTitle(); 
                }
                if (name.equals("Description")){
                  r = cd.getDescription();
                }
                if (name.equals("Functional Name")){
                  r = cd.getFName(); 
                }
                if (name.equals("Timeout")){
                  r = String.valueOf(cd.getTimeout()); 
                }
            } catch (Exception e) {
                log.error( "ChannelRegistryPropertyFinder.getProperty("+entityID.getKey()+"-"+entityID.getType().getName()+","+name+") :", e);
            }
        }
        return  r;
    }
}



