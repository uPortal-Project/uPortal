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
                log.error( "Could not initialize EntityPropertyRegistry");
                log.error( e);
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
         log.error( e);
         Exception ee = e.getRecordedException();
         if (ee != null) {
            log.error( ee);
         }
      }
   }

   public void addToCache(EntityProperties ep) {
      try {
         EntityCachingService.instance().add(ep);
      } catch (CachingException e) {
         log.error( e);
         Exception ee = e.getRecordedException();
         if (ee != null) {
            log.error( ee);
         }
      }
   }

   public EntityProperties getCachedProperties(EntityIdentifier entityID) {
      EntityProperties ep = null;
      try {
         ep = (EntityProperties) EntityCachingService.instance().get(propsType,
                                                                     entityID.getKey());
      } catch (CachingException e) {
         log.error( e);
         Exception ee = e.getRecordedException();
         if (ee != null) {
            log.error( ee);
         }
      }
      return ep;
   }
   
}



