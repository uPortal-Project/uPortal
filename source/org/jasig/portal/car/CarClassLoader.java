/**
 * Copyright © 2002 The JA-SIG Collaborative.  All rights reserved.
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


package org.jasig.portal.car;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.util.StringTokenizer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Loads classes and resources from installed CARs via the CarResources class.
 * If classes are visible via the parent class loader then they will be used
 * in place of those in the CARs. This is a singleton so that we have a single
 * unified class namespace for all car resources preventing linkage errors and
 * class cast exceptions.
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
public class CarClassLoader
    extends SecureClassLoader
{
    public final static String RCS_ID = "@(#) $Header$";

    private static final Log log = LogFactory.getLog(CarClassLoader.class);
    
    /**
       Create a CarClassLoader. This method has package scope so that
       CarResources can instantiate it and hold the single instance to be
       aquired via its getClassLoader() method.
     */
    CarClassLoader()
    {
        super();
    }

    /**
       Create a CarClassloader with the indicated parent class loader. See
       comment for zero parameter constructor for description of package scoping.
     */
    CarClassLoader( ClassLoader cl )
    {
        super( cl );
    }

    /**
       Implement the overloading of findClass to return classes that are
       available from installed CAR files. Class loading precedes with the
       parent classloader first which delegates to this class loader if the
       classes aren't found.
     */
    public Class findClass( final String name )
        throws ClassNotFoundException
    {
        PrivilegedExceptionAction action = new PrivilegedExceptionAction()
            {
                public Object run()
                    throws ClassNotFoundException
                {
                    byte[] buf = null;
                    String pkgName = getPackageName(name);
                    InputStream in = null;
                    try
                    {
                        String file = name.replace( '.', '/' ) + ".class";
                        CarResources crs = CarResources.getInstance();
                        int size = (int) crs.getResourceSize( file );
                        in = crs.getResourceAsStream( file );
                            
                        if ( in == null || size == -1 )
                            throw new Exception( "Car resource " +
                                                 file + " not found." );
                            
                        buf = new byte[size];
                        int offSet = 0;
                        int totalRead = 0;
                        int bytesRead = 0;
                        int remaining = size;

                        while( totalRead < size )
                        {
                            bytesRead = in.read( buf, offSet, remaining );
                            remaining -= bytesRead;
                            offSet += bytesRead;
                            totalRead += bytesRead;
                        } 
                    }
                    catch( Exception e )
                    {
                        throw new ClassNotFoundException( name,
                                                          e );
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException ioe) {
                            log.error(
                                    "CarClassLoader::findClass() Could not close inputStream "
                                            + ioe);
                        }
                    }

                    // package must be defined prior to defined
                    // the class.
                    createPackage( pkgName );
                    
                    return defineTheClass( name, buf, 0, buf.length);
                }
            }; 
        try
        {
            return ( Class ) AccessController.doPrivileged( action );
        }      
        catch( PrivilegedActionException pae )
        {
            throw (ClassNotFoundException) pae.getException();
        }
    }

    /**
       Create and return the Class object from the passed in class bytes. This
       code enables the inner class used in findClass() to call into the
       superclass's defineClass method. It has protected scope in the
       superclass and hence is not visible to an innner class but is visible
       to this class.
     */
    private Class defineTheClass( String n, byte[] b, int offset, int len )
    {
        return super.defineClass( n, b, offset, len );
    }


    /**
     * Creates the package name for the calling class, which is null
     * by default based on the JavaDoc for ClassLoader.  The package
     * must be created prior to defining the Class.
     *
     * @param pkgName the package to create.
     **/
    private void createPackage(String pkgName)
    {
        // package must be defined before the class
        // according to the API docs.
        try
        {
            if ( null != pkgName && null == getPackage(pkgName))
                definePackage( pkgName, "", "", "", "", "", "", null );
        }
        catch( IllegalArgumentException iae )
        {
            // do nothing, assume a synchronization issue
            // where one thread had set it prior to another
            // doing so..  small window, but could happen.
        }
    }
    

    /**
     * Returns a package name from a package/classname path.  If the
     * package is not available (default package), then null is
     * returned.
     *
     * @param name the package/class name.
     * @return the package name (dot notation) or null if not found
     */
    private String getPackageName( String name )
    {
        if ( name.indexOf(".") != -1 )
        {
            StringBuffer sb = new StringBuffer();
            StringTokenizer st = new StringTokenizer(name,".");
            int tokens = st.countTokens();                            
            int count = 1;
            while(st.hasMoreTokens())
            {
                if ( count < tokens )
                {
                    sb.append(st.nextToken());
                    if ( count != (tokens-1) )  
                        sb.append(".");
                }
                else
                    break;
                count++;
            }
            return sb.toString();
        }
        else
            return null;        
    }

    
    /**
       Returns a URL pointing to a car resource if a suitable resource is
       found in the loaded set of CAR files or null if one is not found.
     */
    public URL findResource( String res )
    {
        return CarResources.getInstance().findResource( res );
    }
}
