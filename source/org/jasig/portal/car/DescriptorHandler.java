/**
 * Copyright © 2003 The JA-SIG Collaborative.  All rights reserved.
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
import java.util.Enumeration;
import java.util.Properties;
import java.util.Vector;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.jasig.portal.services.LogService;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Class to parse the component deployment descriptor and provide access to
 * its various parts.
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
class DescriptorHandler
    extends DefaultHandler
{
    public final static String RCS_ID = "@(#) $Header$";

    // variables that act as recipients of descriptor data when pre-loading is
    // specified
    private Properties workers = null;
    private ContentHandler services = null;

    // variables to assist with parsing
    private JarFile jarFile = null;
    private PathRouter currentRouter = null;
    private Path currentPath = null;
    private PathRouter[] routers = null;

    // define ancestry paths in the XML that identify specific sub-tree
    // portions of the xml to be handled by different contentHandlers. This
    // enables us to only use portions of the tree or the whole tree as needed.
    
    public static final Path WORKERS = Path.fromXML("<component><worker>");
    public static final Path SERVICES = Path.fromXML("<component><service>");
    public static final Path EXTENSIONS = Path.fromXML("<component><ext>");

    private static final PathRouter[] ROUTER_ARRAY = new PathRouter[] {};

    // see notes for triggerExtensionProcessing.
    static 
    {
        triggerExtensionProcessing();
    }
    

    /**
       Constructs a new CAR descriptor handler that will parse the descriptor
       for each call to aquire an exposed sub-portions of the descriptor.
     */
    public DescriptorHandler( JarFile jarFile )
    {
        this( jarFile, null );
    }
    
    /**
       Constructs a new CAR descriptor handler that will parse the descriptor
       only once as part of construction and will cache only information for
       the sub-portions specified in pathsToLoad. If any call is made to
       aquire information from a sub-portion not specified the descriptor will
       be parsed again for each call to aquire such information.
    */
    public DescriptorHandler( JarFile jarFile,
                              Path[] pathsToLoad )
    {
        this.jarFile = jarFile;
        this.currentPath = new Path();

        if ( pathsToLoad != null )
        {
            Vector routerSet = new Vector();
        
            // set up routers for handling only the portions of the sub-tree of
            // interest and set up any internal structures for holding the info
            for( int i=0; i<pathsToLoad.length; i++ )
            {
                if ( pathsToLoad[i] == WORKERS )
                {
                    workers = new Properties();
                    WorkerHandler handler = new WorkerHandler( workers );
                    routerSet.add( new PathRouter( WORKERS, handler ) );
                }
                else if ( pathsToLoad[i] == SERVICES )
                {
                    services = new SAX2BufferImpl();
                    routerSet.add( new PathRouter( SERVICES, services ) );
                }
                else if ( pathsToLoad[i] == EXTENSIONS )
                {
                    ExtensionHandler ext = new ExtensionHandler();
                    routerSet.add( new PathRouter( EXTENSIONS, ext ) );
                }
            }
            // routers watch for specific sub-trees in the xml
            // and 1) put a contentHandler in place to receive events for the
            // sub-tree as it is entered, and 2) remove that handler once
            // the parser steps back up out of that sub-tree. Note: handlers
            // receive the events for the first element in their specified
            // sub-tree which corresponds to the last item in the specified
            // router's path.
            routers = (PathRouter[]) routerSet.toArray(ROUTER_ARRAY);
            parseDescriptor();
        }
    }

    /////// Methods for use by CarResources to aquire descriptor info.

    /**
       Triggers processing of extensions declared in CAR deployment
       descrioptors according to proper protocol. Do not change its
       scope nor add calls to it unless you know what you are doing. Extensions
       use the CarClassLoader to load their extensions. The CarResources class
       has two static initializer assignments; one to instantiate a singleton
       instance of CarResources and the other to instantiate a singleton
       instance of CarClassLoader. Instantiating CarResources causes all CARS
       to be located and scanned to build internal tables of what resources
       are available from CARs. Processing of extension blocks in a CAR's
       deployment descriptor should not happen as part of this instantiation
       because the class is being instantiate and hence any calls to instance
       variables will lock including the call to aquire the class loader. Even
       if that call is made to be a static method instantiation of the loader
       will not have taken place and should not until the resources are
       available since it uses the CarResources to aquire its CAR based
       resources. Therefore, processsing of extension blocks should only
       happend after both the CarResoruces and CarClassLoader instances are
       instantiated. This method is constructed to fulfill that requirement.
     */
    private static void triggerExtensionProcessing()
    {
        new Thread()
        {
            public void run()
            {
                this.setName("CAR Descriptor Extensions Processor");
                CarResources cRes = null;
                ClassLoader cLoader = null;
                
                while ( cRes == null || cLoader == null )
                {
                    try
                    {
                        Thread.sleep( 1000 );
                        
                        if ( cRes == null )
                            cRes = CarResources.getInstance();
                        if ( cRes != null )
                            cLoader = cRes.getClassLoader();
                    }
                    catch( InterruptedException ie )
                    {
                    }
                }
                // ok. both resource and loader objects are ready
                // process any extension blocks within descriptors.
                Path[] paths = new Path[] { DescriptorHandler.EXTENSIONS };
                for ( Enumeration jars = cRes.jarsWithDescriptors.elements();
                      jars.hasMoreElements(); )
                    new DescriptorHandler( ((JarFile) jars.nextElement()),
                                           paths );
            }
        }.start();
    }
    
    /**
       Adds to the passed in Properties object the names of workers and their
       implementing classes as specified in the component's deployment
       descriptor.
     */
    public synchronized void getWorkers( Properties p )
    {
        if ( workers == null ) // sub-tree not pre-loaded so parse for it
        {
            WorkerHandler handler = new WorkerHandler( p );
            routers = new PathRouter[] { new PathRouter( WORKERS, handler ) };
            parseDescriptor();
            return;
        }

        // sub-tree pre-loaded so push its values in
        for( Enumeration e=workers.propertyNames(); e.hasMoreElements(); )
        {
            String name = (String) e.nextElement();
            p.put( name, workers.getProperty( name ) );
        }
    }

    /**
       Generates event calls to the passed in handler for service descriptions
       contained in the deployment descriptor.
     */
    public synchronized void getServices( ContentHandler c )
        throws SAXException
    {
        if ( services == null ) // sub-tree not pre-loaded so parse for it
        {
            routers = new PathRouter[] { new PathRouter( SERVICES, c ) };
            parseDescriptor();
            return;
        }

        // sub-tree pre-loaded so push its values into passed in handler
        SAX2BufferImpl buffer = (SAX2BufferImpl) services;
        buffer.stopBuffering();
        buffer.outputBuffer( c );
    }

    /**
       Parse the deployment descriptor for a CAR. No need to synchronize since
       private and only called from synchronized methods.
     */

    private void parseDescriptor()
    {
        ZipEntry entry = jarFile.getEntry(CarResources.DEPLOYMENT_DESCRIPTOR);
        SAXParser parser = null;
        InputStream is = null;
        
        if ( entry == null ) // should never happen
            return;
            
        try
        {
            is = jarFile.getInputStream( entry );
            parser = SAXParserFactory.newInstance().newSAXParser();
        }
        catch( ZipException ze )
        {
            LogService.log( LogService.DEBUG,
                            "The zip entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            " has an invalid format. Details: " + ze );
        }
        catch( IOException ioe )
        {
            LogService.log( LogService.DEBUG,
                            "Unable to read entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            ". Details: " + ioe );
        }
        catch( SecurityException se )
        {
            LogService.log( LogService.DEBUG,
                            "Unable to read entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            " because some entries are incorrectly signed. " +
                            "Details: " + se );
        }
        catch( FactoryConfigurationError fce )
        {
            LogService.log( LogService.DEBUG,
                            "Unable to read entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            " because a parser factory could not be created." +
                            " Details: " + fce );
        }
        catch( ParserConfigurationException pce )
        {
            LogService.log( LogService.DEBUG,
                            "Unable to read entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            " because a parser could not be created." +
                            " Details: " + pce );
        }
        catch( SAXException sxe )
        {
            LogService.log( LogService.DEBUG,
                            "Unable to read entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            " because a parser could not be created." +
                            " Details: " + sxe );
        }
        
        try
        {
            // can throw SAXException, IOException
            parser.parse( is, this );
        }
        catch( RuntimeException re )
        {
            LogService.log( LogService.DEBUG,
                            "Unable to completely parse entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            " because a fatal parser error occurred. " +
                            "Details: " + re.getMessage() );
        }
        catch( IOException ioe )
        {
            LogService.log( LogService.DEBUG,
                            "Unable to completely parse entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            ". Details: " + ioe );
        }
        catch( SAXException sxe )
        {
            LogService.log( LogService.DEBUG,
                            "Unable to completely parse entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            ". Details: " + sxe );
        }
        finally 
        {
			try 
			{
				if(is != null)
			    is.close();
			} 
			catch (IOException ioe) {
				LogService.log(LogService.ERROR,
						"::Unable to close inputStream " + ioe);
			}
		}
    }

    /////// ErrorHandler interface methods
    public void error(SAXParseException exception)
        throws SAXException
    {
        LogService.log( LogService.DEBUG,
                        "A non-fatal parsing error occurred while parsing " +
                        CarResources.DEPLOYMENT_DESCRIPTOR +
                        " in " + jarFile.getName() + ". Details: " +
                        exception );
    }
    
    public void fatalError(SAXParseException exception)
        throws SAXException
    {
        throw new RuntimeException( exception.toString() );
    }

    public void warning(SAXParseException exception)
        throws SAXException
    {
        LogService.log( LogService.DEBUG,
                        "A parsing warning occurred while parsing " +
                        CarResources.DEPLOYMENT_DESCRIPTOR +
                        " in " + jarFile.getName() + ". Details: " +
                        exception );
    }
    
    /////// ContentHandler methods of interest

    public void startElement(java.lang.String namespaceURI,
                         java.lang.String localName,
                         java.lang.String qName,
                         Attributes atts)
        throws SAXException
    {
        // add to the path as we traverse the xml tree.
        currentPath.append( qName );

        // if no one is handling this portion of the sub tree see if anyone.
        // should.
        if ( currentRouter == null )
            for( int i=0; currentRouter == null && i<routers.length; i++ )
                if ( routers[i].looksFor( currentPath ) )
                    currentRouter = routers[i];

        // if anyone is handling, pass the event on to them.
        if ( currentRouter != null )
            currentRouter.handler.startElement( namespaceURI, localName,
                                                qName, atts );
    }
    
    public void endElement(java.lang.String namespaceURI,
                       java.lang.String localName,
                       java.lang.String qName)
                throws SAXException
    {
        // if anyone is handling, pass the event on to them.
        if ( currentRouter != null )
        {
            currentRouter.handler.endElement(namespaceURI, localName, qName);

            // now see if we are leaving the sub-tree handled by current
            // router's handler.
            if ( currentRouter.looksFor( currentPath ) )
                currentRouter = null;
        }

        // drop the last item from the list. should be same as localName.
        currentPath.removeLast();
    }

    public void characters(char[] ch,
                       int start,
                       int length)
        throws SAXException
    {
        if ( currentRouter != null )
            currentRouter.handler.characters( ch, start, length );
    }

    ////// Utility classes /////////////////////////////////

    /**
       Handles reading the worker tags in the descriptor. These tags contain
       only attributes and hence only the startElement event is needed.
     */
    private class WorkerHandler
        extends DefaultHandler
    {
        private Properties workerProps = null;
        
        WorkerHandler( Properties p )
        {
            workerProps = p;
        }
        
        public void startElement(java.lang.String namespaceURI,
                                 java.lang.String localName,
                                 java.lang.String qName,
                                 Attributes atts)
            throws SAXException
        {
            if ( !qName.equals( "worker" ) )
                return;
            String workerClass = atts.getValue( "class" );
        
            if ( workerClass == null )
                return;
        
            workerProps.put( workerClass.replace( '.', '_' ), workerClass );
        }
    }

    /**
       Handles reading the extension tags in the descriptor. These tags contain
       extension specific content that is foreign to the deployment descriptor
       but understood by the handler class declared by the extension tag. Upon
       entering an extension block the declared handler class will be
       instantiated, the startDocument method will be call indicating to the
       class that parsing of the content of its extension block will be
       forthcoming. All events within the block are then passed to the handler
       until the block is exited at which point the endDocument method is
       called. Neither the startElement nor the endElement events for the
       enclosing extension element are passed to the handler class. It only
       sees events for content within the block.
     */
    private class ExtensionHandler
        extends DefaultHandler
    {
        ContentHandler extHandler = null;

        /**
           Passes character events to an extension block's declared handler
           if within an extension block and the handler was successfully
           instantiated.
         */
        public void characters(char[] ch,
                               int start,
                               int length)
            throws SAXException
        {
            if ( extHandler != null )
                extHandler.characters( ch, start, length );
        }

        /**
           Receives startElement events watching for entry into an extension
           block so that it can instantiate a declared handler for that block
           and pass events within the block to that handler.
         */
        public void startElement(String namespaceURI,
                                 String localName,
                                 String qName,
                                 Attributes atts)
            throws SAXException
        {
            if ( extHandler != null )
                extHandler.startElement( namespaceURI, localName, qName, atts);
            
            if ( qName.equals( "ext" ) &&
                 currentPath.equals( EXTENSIONS ) ) 
            {
                // entering ext block so clean out old if around, make sure
                // that we haven't already processed this guy, and
                // get the handler for this block ready to roll
                extHandler = null;
                
                String handlerClass = atts.getValue( "contentHandler" );
        
                if ( handlerClass == null )
                {
                    LogService.log( LogService.ERROR,
                                    "Ignoring invalid extension while " +
                                    "processing deployment descriptor for " + 
                                    jarFile.getName() +
                                    ". Attribute 'contentHandler' must be " +
                                    "specified." );
                    return;
                }
                // now lets load and instantiate the handler
                Class c = null;
                Object obj = null;
                
                try
                {
                    CarResources cRes = CarResources.getInstance();
                    ClassLoader cl = cRes.getClassLoader();
                    c = cl.loadClass( handlerClass );
                }
                catch( ClassNotFoundException clfe )
                {
                    LogService.log( LogService.ERROR,
                                    "Specified contentHandler class " +
                                    handlerClass +
                                    " not found. Ignoring extension block " +
                                    "in deployment descriptor of " +
                                    jarFile.getName() + "." );
                    return;
                }
                try
                {
                    obj = c.newInstance();
                }
                catch( Exception e )
                {
                    LogService.log( LogService.ERROR,
                                    "Unable to create specified " +
                                    "contentHandler class " + handlerClass +
                                    " for extension block" + 
                                    " in deployment descriptor of " +
                                    jarFile.getName() + 
                                    ". Ignoring extension. Details: " + e );
                    return;
                }
                try
                {
                    extHandler = (ContentHandler) obj;
                }
                catch( ClassCastException cce )
                {
                    LogService.log( LogService.ERROR,
                                    "Specified contentHandler class " +
                                    handlerClass +
                                    " for extension in deployment " +
                                    "descriptor of " + jarFile.getName() + 
                                    " does not implement ContentHandler. " + 
                                    "Ignoring extension." );
                    return;
                }

                extHandler.startDocument();
            }
        }
        /**
           Receives end element calls passing them to the declared handler if
           within an extension block or removing the current handler when an
           extension block is exited.
         */
        public void endElement(String namespaceURI,
                               String localName,
                               String qName)
            throws SAXException
        {
            if ( qName.equals( "ext" ) &&
                 currentPath.equals( EXTENSIONS ) ) 
            {
                // leaving ext block so clean tell the handler that its
                // document is finished and then remove the handler.
                if ( extHandler != null )
                    extHandler.endDocument();

                extHandler = null;
            }

            // while within ext block handler will be non-null and should
            // receive all events.
            if ( extHandler != null )
                extHandler.endElement( namespaceURI, localName, qName );
        }
    }
    
    
    /**
       Holds a Path object for which is watches and answers true when that path
       is seen. Also holds onto the handler for the sub-tree below that path.
     */
    private class PathRouter
    {
        Path pathLookedFor = null;
        ContentHandler handler = null;

        PathRouter( Path p, ContentHandler handler )
        {
            this.pathLookedFor = p;
            this.handler = handler;
        }

        ContentHandler handler()
        {
            return handler;
        }
        
        boolean looksFor( Path aPath )
        {
            return pathLookedFor.equals( aPath );
            
        }
    }
}


