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

import  org.jasig.portal.concurrency.IBasicEntity;
import  org.jasig.portal.*;
import  org.jasig.portal.services.entityproperties.IEntityPropertyStore;
import  org.jasig.portal.utils.*;
import  org.w3c.dom.*;
import  java.util.*;
import  org.jasig.portal.groups.*;
import  org.jasig.portal.services.entityproperties.*;
import  org.jasig.portal.concurrency.*;
import  org.apache.xpath.*;


/**
 * A Service to allow the querying and storing of properties relating
 * to portal entities.  Configured using /properties/EntityPropertyRegistry.xml
 *
 * see dtds/EntityPropertyRegistry.dtd for configuration file grammar
 *
 * @author Alex Vigdor av317@columbia.edu
 * @version $Revision$
 */
public class EntityPropertyRegistry {
    protected static EntityPropertyRegistry _instance;
    protected IEntityPropertyStore store;
    protected int storePrecedence;
    protected IEntityPropertyFinder[] finders;
    protected Class[] finderTypes;
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
        finderTypes = new Class[top + 1];
        for (int i = 0; i < ff.getLength(); i++) {
            Element f = (Element)ff.item(i);
            finders[Integer.parseInt(f.getAttribute("precedence"))] = (IEntityPropertyFinder)Class.forName(f.getAttribute("impl")).newInstance();
            finderTypes[Integer.parseInt(f.getAttribute("precedence"))] = Class.forName(f.getAttribute("type"));
        }
        propsType = Class.forName("org.jasig.portal.services.entityproperties.EntityProperties");
    }

    protected synchronized static EntityPropertyRegistry instance() {
        if (_instance == null) {
            try {
                _instance = new EntityPropertyRegistry();
                _instance.init();
            } catch (Exception e) {
                _instance = null;
                LogService.instance().log(LogService.ERROR, "Could not initialize EntityPropertyRegistry");
                LogService.instance().log(LogService.ERROR, e);
            }
        }
        return  _instance;
    }

    public static String[] getPropertyNames(IBasicEntity entity) {
        return  instance().getProperties(entity).getPropertyNames();
    }

    public static String getProperty(IBasicEntity entity, String name) {
        return  instance().getProperties(entity).getProperty(name);
    }

    public static void storeProperty(IBasicEntity entity, String name, String value) {
        instance().store.storeProperty(entity, name, value);
        instance().clearCache(entity);
    }

    public static void unStoreProperty (IBasicEntity entity, String name) {
        instance().store.unStoreProperty(entity, name);
        instance().clearCache(entity);
    }

    protected void clearCache(IBasicEntity entity) {
        try {
            EntityCachingService.instance().remove(propsType, getPropKey(entity));
        } catch (CachingException e) {
            LogService.instance().log(LogService.ERROR, e);
            Exception ee = e.getRecordedException();
            if (ee != null) {
                LogService.instance().log(LogService.ERROR, ee);
            }
        }
    }

    protected String getPropKey(IBasicEntity entity) {
        return  org.jasig.portal.EntityTypes.getEntityTypeID(entity.getEntityType()).toString()
                + "." + entity.getEntityKey();
    }

    protected EntityProperties getProperties(IBasicEntity entity) {
        EntityProperties ep = null;
        try {
            ep = (EntityProperties)EntityCachingService.instance().get(propsType,
                    getPropKey(entity));
        } catch (CachingException e) {
            LogService.instance().log(LogService.ERROR, e);
            Exception ee = e.getRecordedException();
            if (ee != null) {
                LogService.instance().log(LogService.ERROR, ee);
            }
        }
        if (ep == null) {
            ep = new EntityProperties(getPropKey(entity));
            for (int i = 0; i < finders.length; i++) {
                IEntityPropertyFinder finder;
                if (i == storePrecedence) {
                    finder = store;
                }
                else {
                    if (entity.getEntityType().equals(finderTypes[i])) {
                        finder = finders[i];
                    }
                    else {
                        finder = null;
                    }
                }
                if (finder != null) {
                    String[] names = finder.getPropertyNames(entity);
                    for (int j = 0; j < names.length; j++) {
                        ep.setProperty(names[j], finder.getProperty(entity,
                                names[j]));
                    }
                }
            }
            try {
                EntityCachingService.instance().add(ep);
            } catch (CachingException e) {
                LogService.instance().log(LogService.ERROR, e);
                Exception ee = e.getRecordedException();
                if (ee != null) {
                    LogService.instance().log(LogService.ERROR, ee);
                }
            }
        }
        return  ep;
    }
}



