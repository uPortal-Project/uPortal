/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/


package  org.jasig.portal.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.EntityIdentifier;
import org.jasig.portal.concurrency.CachingException;
import org.jasig.portal.services.entityproperties.EntityProperties;
import org.jasig.portal.services.entityproperties.IEntityPropertyFinder;
import org.jasig.portal.services.entityproperties.IEntityPropertyStore;
import org.jasig.portal.utils.ResourceLoader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * A Service to allow the querying and storing of properties relating
 * to portal entities.  Configured using /properties/EntityPropertyRegistry.xml
 *
 * see dtds/EntityPropertyRegistry.dtd for configuration file grammar
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 *
 * @author Don Fracapane df7@columbia.edu
 * Removed caching from this class and delegated it to the finder classes. Each
 * finder can choose the method of caching if caching is appropriate.
 */
public class EntityPropertyRegistry {
    
    private static final Log log = LogFactory.getLog(EntityPropertyRegistry.class);
    
    protected static EntityPropertyRegistry _instance;
    protected IEntityPropertyStore store;
    protected int storePrecedence;
    protected IEntityPropertyFinder[] finders;
    protected Object[] finderTypes;
    protected Class propsType;

    protected EntityPropertyRegistry() {
    }

    protected void init() throws Exception {
        Document def = ResourceLoader.getResourceAsDocument(this.getClass(),
                "/properties/EntityPropertyRegistry.xml");
        NodeList ss = def.getElementsByTagName("store");
        if (ss.getLength() == 1) {
            Element s = (Element)ss.item(0);
            this.store = (IEntityPropertyStore)Class.forName(s.getAttribute("impl")).newInstance();
            this.storePrecedence = Integer.parseInt(s.getAttribute("precedence"));
        }
        NodeList ff = def.getElementsByTagName("finder");
        int top = storePrecedence;
        for (int i = 0; i < ff.getLength(); i++) {
            Element f = (Element)ff.item(i);
            int test = Integer.parseInt(f.getAttribute("precedence"));
            if (test > storePrecedence) {
                top = test;
            }
        }
        finders = new IEntityPropertyFinder[top + 1];
        finderTypes = new Object[top + 1];
        for (int i = 0; i < ff.getLength(); i++) {
            Element f = (Element)ff.item(i);
            int p = Integer.parseInt(f.getAttribute("precedence"));
            finders[p] = (IEntityPropertyFinder)Class.forName(f.getAttribute("impl")).newInstance();
            String type = f.getAttribute("type");
            if (type.equals("*")){
              finderTypes[p] = type;
            }
            else{
              finderTypes[p] = Class.forName(type);
            }
        }
        propsType = Class.forName("org.jasig.portal.services.entityproperties.EntityProperties");
    }

    public synchronized static EntityPropertyRegistry instance() {
        if (_instance == null) {
            try {
                _instance = new EntityPropertyRegistry();
                _instance.init();
            } catch (Exception e) {
                _instance = null;
                log.error( "Could not initialize EntityPropertyRegistry", e);
            }
        }
        return  _instance;
    }

    public static String[] getPropertyNames(EntityIdentifier entityID) {
        return  instance().getProperties(entityID).getPropertyNames();
    }

    public static String getProperty(EntityIdentifier entityID, String name) {
        return  instance().getProperties(entityID).getProperty(name);
    }

    public static void storeProperty(EntityIdentifier entityID, String name, String value) {
        instance().store.storeProperty(entityID, name, value);
    }

    public static void unStoreProperty (EntityIdentifier entityID, String name) {
        instance().store.unStoreProperty(entityID, name);
    }

    protected String getPropKey(EntityIdentifier entityID) {
       String key = org.jasig.portal.EntityTypes.getEntityTypeID(entityID.getType()).toString()
                + "." + entityID.getKey(); 
       return  key;
    }

   protected EntityProperties getProperties(EntityIdentifier entityID) {
      EntityProperties ep = null;
      ep = new EntityProperties(getPropKey(entityID));
      for (int i = 0; i < finders.length; i++) {
         IEntityPropertyFinder finder;
         if (i == storePrecedence) {
            finder = store;
         }
         else {
            if ((finderTypes[i]!=null) && (finderTypes[i].equals("*") || entityID.getType().equals(finderTypes[i]))) {
               finder = finders[i];
            }
            else {
               finder = null;
            }
         }
         if (finder != null) {
            String[] names = finder.getPropertyNames(entityID);
            for (int j = 0; j < names.length; j++) {
               ep.setProperty(names[j], finder.getProperty(entityID,
                       names[j]));
            }
         }
      }
      return  ep;
   }

   public void clearCache(EntityIdentifier entityID) {
      try {
         EntityCachingService.instance().remove(propsType, getPropKey(entityID));
      } catch (CachingException e) {
         log.error("Error clearing cache for entity ID [" + entityID + "]", e);
         Exception ee = e.getRecordedException();
         if (ee != null) {
            log.error(ee, ee);
         }
      }
   }

   public void addToCache(EntityProperties ep) {
      try {
         EntityCachingService.instance().add(ep);
      } catch (CachingException e) {
         log.error("Error adding entity properties [" + ep + "] to the cache", e);
         Exception ee = e.getRecordedException();
         if (ee != null) {
            log.error(ee, ee);
         }
      }
   }

   public EntityProperties getCachedProperties(EntityIdentifier entityID) {
      EntityProperties ep = null;
      try {
         ep = (EntityProperties) EntityCachingService.instance().get(propsType,
                                                                     entityID.getKey());
      } catch (CachingException e) {
         log.error("Error getting cached properties for entity [" + entityID + "]", e);
         Exception ee = e.getRecordedException();
         if (ee != null) {
            log.error(ee, ee);
         }
      }
      return ep;
   }
   
}



