/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.services.dom;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.jasig.portal.PortalException;
import org.jasig.portal.ResourceMissingException;
import org.jasig.portal.utils.ResourceLoader;

/**
 * This provides a DOM initialization service interface. It uses reflection
 * so the resin library doesn't need to be available at compile time.
 * @author Nick Bolton, nbolton@unicon.net
 * @version $Revision$
 */
public final class ResinInitServiceImpl implements IDOMInitService {

    ResinInitServiceImpl() {}

    /**
     * Executes an initialization procedure for a specific dom implementation.
     */
    public void initialize() throws PortalException {
        try {
            Method method;
            Object[] params = new Object[1];
            Class[] paramClasses = new Class[1];

            File regFile = ResourceLoader.getResourceAsFile(this.getClass(),
                "/properties/resin.conf");
            FileInputStream is = new FileInputStream(regFile);

            // create the com.caucho.vfs.FileReadStream object
            params[0] = is;
            paramClasses[0] = FileInputStream.class;
            Object fileReadStream =
                Class.forName("com.caucho.vfs.FileReadStream").
                    getDeclaredConstructor(paramClasses).newInstance(params);
            
            // create the com.caucho.vfs.ReadStream object
            params[0] = fileReadStream;
            paramClasses[0] = Class.forName("com.caucho.vfs.StreamImpl");
            Object readStream =
                Class.forName("com.caucho.vfs.ReadStream").
                    getDeclaredConstructor(paramClasses).newInstance(params);

            // parse resin configuration
            params[0] = readStream;
            paramClasses[0] = Class.forName("com.caucho.vfs.ReadStream");
            method = Class.forName("com.caucho.util.Registry").
                getDeclaredMethod("parse", paramClasses);
            Object registry = method.invoke(null, params);

            // set configuration as the default
            params[0] = registry;
            paramClasses[0] = Class.forName("com.caucho.util.Registry");
            method = Class.forName("com.caucho.util.Registry").
                getDeclaredMethod("setDefault", paramClasses);
            method.invoke(null, params);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            throw new PortalException(ioe);
        } catch (InvocationTargetException ite) {
            ite.printStackTrace();
            throw new PortalException(ite);
        } catch (InstantiationException ie) {
            ie.printStackTrace();
            throw new PortalException(ie);
        } catch (IllegalAccessException iae) {
            iae.printStackTrace();
            throw new PortalException(iae);
        } catch (NoSuchMethodException nsme) {
            nsme.printStackTrace();
            throw new PortalException(nsme);
        } catch (ResourceMissingException rme) {
            rme.printStackTrace();
            throw new PortalException(rme);
        } catch (ClassNotFoundException cnfe) {
            cnfe.printStackTrace();
            throw new PortalException(cnfe);
        }
    }
}
