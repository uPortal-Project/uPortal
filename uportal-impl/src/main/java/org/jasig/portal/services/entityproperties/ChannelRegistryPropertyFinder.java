/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package  org.jasig.portal.services.entityproperties;

import org.jasig.portal.ChannelDefinition;
import org.jasig.portal.ChannelRegistryStoreFactory;
import org.jasig.portal.EntityIdentifier;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


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
   protected static Class chan = null;
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
      try {
         chan = Class.forName("org.jasig.portal.ChannelDefinition");
      } catch (Exception e) {
         log.error( "ChannelRegistryPropertyFinder - static:", e);
      }
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
                ChannelDefinition cd = ChannelRegistryStoreFactory.getChannelRegistryStoreImpl().getChannelDefinition(Integer.parseInt(entityID.getKey()));
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
                if (name.equals("Secure")){
                  r = String.valueOf(cd.isSecure());
                }                
            } catch (Exception e) {
                log.error( "ChannelRegistryPropertyFinder.getProperty("+entityID.getKey()+"-"+entityID.getType().getName()+","+name+") :", e);
            }
        }
        return  r;
    }
}



