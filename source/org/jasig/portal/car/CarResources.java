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

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Map;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.servlet.Servlet;
import javax.servlet.ServletContext;

import org.jasig.portal.PortalException;
import org.jasig.portal.PortalSessionManager;
import org.jasig.portal.PropertiesManager;
import org.jasig.portal.services.LogService;

/**
   Provides access to resources stored in channel archive files or CARs for
   short. 
   @author Mark Boyd <mark.boyd@engineer.com>
 */
public class CarResources {

    private static CarResources instance = new CarResources();
    public final static String RCS_ID = "@(#) $Header$";
    
    private static final String WELL_KNOWN_DIR = "/WEB-INF/cars";
    private static final String CAR_DIR_PROP_NAME = "org.jasig.portal.car.CarResources.directory";

    private Hashtable resourceJars = new Hashtable();
    private Hashtable carsByJars = new Hashtable();
    private Hashtable carContents = new Hashtable();
    private Hashtable carsByPath = new Hashtable();
    
    private String carDirPath = null;
    private boolean carDirExists = false;
    
    public static final String CAR_WORKER_ID = "carRsrc";
    public static final String CAR_RESOURCE_PARM = CAR_WORKER_ID;
    private static final Map.Entry[] ENTRY_ARRAY = new Map.Entry[] {};
    private static final String[] STRING_ARRAY = new String[] {};

    /**
       A fileFilter for obtaining a list of CARs.
     */
    private FileFilter carFilter = new FileFilter()
    {
        public boolean accept( File path )
        {
            return path.getName().endsWith( ".car" );
        }
    };

    /**
       A fileFilter for obtaining a list of directories.
     */
    private FileFilter dirFilter = new FileFilter()
    {
        public boolean accept( File file )
        {
            return file.isDirectory();
        }
    };

    /**
       Instantiate a CarResources object and load information about all CARs
       and their contained resources.
     */
    private CarResources()
    {
	try
	{
	    loadCars();
	}
	catch( Exception e )
	{
	    StringWriter sw = new StringWriter();
	    PrintWriter p = new PrintWriter( sw );
	    e.printStackTrace( p );
	    p.flush();
	    LogService.log( LogService.ERROR,
				       "An Exception occurred while loading " +
				       "channel archives. Any channels " +
				       "deployed via CARs will not be " +
				       "available. Details:\n"
				       + sw );
	}
    }

    /**
       Return the single instance of CarResources or create if it doesn't
       exist.
     */
    public static CarResources getInstance()
    {
        return instance;
    }

    /**
       Return a File object representing the well-known channel archive base
       directory '/WEB-INF/cars' where channel archives are located.
     */
    private File getWellKnownDir()
    {
        Servlet servlet = PortalSessionManager.getInstance();
        ServletContext ctx = servlet.getServletConfig().getServletContext();
        String carDirRealPath = ctx.getRealPath( WELL_KNOWN_DIR );

        if ( carDirRealPath == null )
        {
            LogService.log( LogService.ERROR,
                                       "Channel Archives will not be " +
                                       " loaded. Unable to aquire the real " +
				       "path to '" + WELL_KNOWN_DIR +
				       "'. This " +
				       "can occur if the portal is deployed " +
				       "as a WAR and directories can not be " +
				       "created within its directory " +
				       "structure. Alternatively, you can " +
				       "specify a fully qualified path as " +
				       "the value of a '" + CAR_DIR_PROP_NAME +
				       "' property in portal.properties." );
            return null;
        }
        
        File carDir = new File( carDirRealPath );
       
        if ( ! carDir.exists() )
        {
            LogService.log( LogService.ERROR,
                                       "Channel Archives can not be " +
                                       " loaded. CAR directory '" +
                                       carDirRealPath + "' does not exist." );
            return null;
        }
        carDirExists = true;
	this.carDirPath = carDirRealPath;
        return carDir;
    }

    /**
       Return a File object representing the channel archive base
       directory whose fully-qualified path is specified by the 
       'org.jasig.portal.car.CarResources.directory' property in
       portal.properties.
     */
    private File getPropertySpecifiedDir()
    {
        String carDirPath = null;
	File carDir = null;
	
        try
        {
            carDirPath = PropertiesManager.getProperty( CAR_DIR_PROP_NAME );
	    carDir = new File( carDirPath );
        }
        catch( RuntimeException re )
        {
            LogService.log( LogService.ERROR,
                                       "CAR directory property '" +
				       CAR_DIR_PROP_NAME +
				       "' not specified. Defaulting to " +
				       "well-known directory '" +
				       WELL_KNOWN_DIR + "'." );
	    return null;
        }

        if ( ! carDir.exists() )
        {
            LogService.log( LogService.ERROR,
                                       "CAR directory '" + carDirPath +
				       "' specified by property '" +
				       CAR_DIR_PROP_NAME +
				       "' does not exist. " +
				       "Channel Archives can not be " +
                                       "loaded from this directory." );
            return null;
        }
        carDirExists = true;
	this.carDirPath = carDirPath;
        return carDir;
    }

    /**
       Load information about all installed CARs and their contained resources.
     */
    private void loadCars()
    {
	File carDir = getPropertySpecifiedDir();
	
	if ( carDir == null )
	    carDir = getWellKnownDir();
	
	if ( carDir != null )
	{    
	    scanDir( carDir );
	    LogService.log( LogService.INFO,
				       "Channel Archives Loaded: " +
				       carsByPath.size() +
				       " from '" + this.carDirPath + "'" );
	}
    }

    /**
       Scan the passed in directory loading any cars there-in and calling
       this method for any nested directories.
     */
    private void scanDir( File dir )
    {
        
        // first get all of the cars in this directory
        File[] cars = dir.listFiles( carFilter );
        
        if ( cars != null && cars.length != 0 )
            for( int i=0; i<cars.length; i++ )
                loadCarEntries( cars[i] );

        // now get all of the sub-directories to be scanned
        File[] dirs = dir.listFiles( dirFilter );
        
        if ( dirs != null && dirs.length != 0 )
            for( int i=0; i<dirs.length; i++ )
                scanDir( dirs[i] );
    }

    /**
       Load information about the passed in CAR and any contained resources.
     */
    private void loadCarEntries( File car )
    {
        JarFile jar = null;

        try
        {
            jar = new JarFile( car );
        }
        catch( IOException ioe )
        {
            LogService.log( LogService.ERROR,
                                       "CAR " + getCarPath( car ) +
                                       " could not be loaded. Details: " +
                                       ( ioe.getMessage() != null ?
                                         ioe.getMessage() :
                                         ioe.getClass().getName() ) );
            return;
        }
        Vector entryList = new Vector();
        carsByJars.put( jar, car );
        Enumeration entries = jar.entries();

        while( entries.hasMoreElements() )
        {
            ZipEntry entry = (ZipEntry) entries.nextElement();
            
            if ( ! entry.isDirectory() )
            {
                String name = entry.getName();
                resourceJars.put( name, jar );
                entryList.add( name );
            }
        }
        carContents.put( car, entryList );
        carsByPath.put( getCarPath( car ), car );
    }

    /**
       Return an input stream for reading the raw bytes making up the resource
       contained in one of the installed CARs. Returns null if the resource
       is not found.
       
     */
    public InputStream getResourceAsStream( String resource )
        throws PortalException
    {
        JarFile jar = (JarFile) resourceJars.get( resource );

        if ( jar == null )
            return null;

        ZipEntry entry = jar.getEntry( resource );

        if ( entry == null )
            return null;

        try
        {
            return jar.getInputStream( entry );
        }
        catch( IOException ioe )
        {
            throw new PortalException( "Unable to get input stream for " +
                                       resource );
        }
    }

    /**
       Return the size of the indicated resource or -1 if the resource is not
       found or its size is unknown.
     */
    public long getResourceSize( String resource )
    {
        JarFile jar = (JarFile) resourceJars.get( resource );

        if ( jar == null )
            return -1;

        ZipEntry entry = jar.getEntry( resource );

        if ( entry == null )
            return -1;
        return entry.getSize();
    }

    /**
       Returns a URL to the requested entry if found in one of the installed
       CARs or null if not found.
     */
    public URL findResource( String entry )
    {
        if ( entry == null )
            return null;
        JarFile jar = (JarFile) resourceJars.get( entry );
        if ( jar == null )
            return null;
        File carFile = (File) carsByJars.get( jar );
        if ( carFile == null ) // should never happen!
            return null;
        String url = "jar:file:" + carFile.getAbsolutePath() + "!/" + entry;
        try
        {
            return new URL( url );
        }
        catch( java.net.MalformedURLException me )
        {
        }
        return null;
    }

    /**
       Returns the path of the CAR containing the indicated resource. This
       path is relative to the CAR directory configured via the property in
       portal.properties. If a CAR for that entry is not found it
       returns null.
     */
    public String getContainingCarPath( String entry )
    {
        if ( entry == null )
            return null;
        JarFile jar = (JarFile) resourceJars.get( entry );
        if ( jar == null )
            return null;
        File carFile = (File) carsByJars.get( jar );
        if ( carFile == null ) // should never happen!
            return null;
        return getCarPath( carFile );
    }

    /**
       Returns true if the indicated resource is available, false otherwise.
       The resource is identified by its complete path within the CAR file.
     */
    public boolean containsResource( String resource )
    {
        return resourceJars.containsKey( resource );
    }

    /**
       Returns a String array of car file paths relative to the car directory
       specified via the property in portal.properties.
     */
    public String[] listCars()
    {
        Map.Entry[] entries = null;
        
        entries = (Map.Entry[]) carsByJars.entrySet().toArray( ENTRY_ARRAY );
        String[] carNames = new String[entries.length];
        
        for( int i=0; i<entries.length; i++ )
            carNames[i] = getCarPath( (File) entries[i].getValue() );
        return carNames;
    }

    /**
       Returns a list of resources available in the car identified by the
       passed in relative car file path name. This name is the path to the
       car file relative to the car directory. If no car file is found for
       the passed-in path then null is returned.
     */
    public String[] listCarResources( String carPath )
    {
        File car = (File) carsByPath.get( carPath );
        if ( car == null )
            return null;
        
        Vector contents = (Vector) carContents.get( car );

        if ( contents == null )
            return null; // should never happen

        return (String[]) contents.toArray( STRING_ARRAY );
    }
    
    /**
       Return the path of a car file relative to the car directory.
     */
    private String getCarPath( File car )
    {
        String carPath = car.getAbsolutePath();
        return carPath.substring( carDirPath.length() + 1 );
    }
    
    /**
       Returns an enumeration of String objects each containing the path of a
       resource available from the installed CARs.
     */
    public String[] listAllResources()
    {
        return (String[]) resourceJars.keySet().toArray( STRING_ARRAY );
    }
}
