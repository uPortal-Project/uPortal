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

import java.io.InputStream;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;

/**
   Loads classes and resources from installed CARs via the CarResources class.
   If classes are visible via the parent class loader then they will be used
   in place of those in the CARs.
   @author Mark Boyd <mark.boyd@engineer.com>
 */
public class CarClassLoader
    extends SecureClassLoader
{
    public final static String RCS_ID = "@(#) $Header$";

    /**
       Create a CarClassLoader.
     */
    public CarClassLoader()
    {
        super();
    }

    /**
       Create a CarClassloader with the indicated parent class loader.
     */
    public CarClassLoader( ClassLoader cl )
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
                    try
                    {
                        String file = name.replace( '.', '/' ) + ".class";
                        CarResources crs = CarResources.getInstance();
                        int size = (int) crs.getResourceSize( file );
                        InputStream in = crs.getResourceAsStream( file );
                            
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
                    }
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
       Returns a URL pointing to a car resource if a suitable resource is
       found in the loaded set of CAR files or null if one is not found.
     */
    public URL findResource( String res )
    {
        return CarResources.getInstance().findResource( res );
    }
}
