/* Copyright 2004 The JA-SIG Collaborative.  All rights reserved.
 *  See license distributed with this file and
 *  available online at http://www.uportal.org/license.html
 */

package org.jasig.portal.tools.dbloader;

import java.lang.reflect.Constructor;

import org.jasig.portal.PortalException;
import org.xml.sax.helpers.DefaultHandler;

class DataHandlerFactory
{
    private static DataHandlerFactory __instance;
    private static final String propName =
        "org.jasig.portal.tools.dbloader.dataHandler.implementation";
    private static final Class defaultDataHandler = DataHandler.class;
    
    public static DataHandlerFactory instance() {
        if (__instance == null) {
            __instance = new DataHandlerFactory();
        }
        return __instance;
    }
    
    public DefaultHandler getHandler(Configuration config) throws PortalException {
        Class dataHandlerClass = null;
        String className = null;
        try {
            DefaultHandler dataHandler = null;
            className = System.getProperty(propName);
            if (className == null) {
                dataHandlerClass = defaultDataHandler;
            } else {
                dataHandlerClass = Class.forName(className);
            }
            Class[] params = {Configuration.class};
            Object[] args = {config};
            Constructor constructor = dataHandlerClass.getConstructor(params);
            dataHandler = (DefaultHandler) constructor.newInstance(args);
            return dataHandler;
        } catch (ClassNotFoundException cnfe) {
            StringBuffer sb = new StringBuffer();
            sb.append("DataHandlerFactory: Could not instantiate ");
            sb.append(className);
            throw new PortalException(sb.toString(), cnfe);
        } catch (Exception e) {
            StringBuffer sb = new StringBuffer();
            sb.append("DataHandlerFactory: Could not instantiate ");
            sb.append(dataHandlerClass);
            throw new PortalException(sb.toString(), e);
        }
    }
    
    DataHandlerFactory()
    {
    }
    
}
