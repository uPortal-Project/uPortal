/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.car;

import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;

import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Class to parse the component deployment descriptor causing some tags to 
 * take action during parsing and others to cache information in the 
 * descriptor making it accessible via accessor methods.
 *
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
 * @version $Revision$
 */
public class DescriptorHandler
{
    public final static String RCS_ID = "@(#) $Header$";
    private static final Log log = LogFactory.getLog(DescriptorHandler.class);
    // variables that act as recipients of descriptor data
    private Properties workers = new Properties();
    private SAX2BufferImpl services = new SAX2BufferImpl();
    
    // define the supported tags
    public static final String COMPONENT_TAG_NAME = "component";
    public static final String WORKER_TAG_NAME = "worker";
    public static final String SERVICE_TAG_NAME = "service";
    public static final String EXTENSION_TAG_NAME = "ext";
    public static final String CHANDEF_TAG_NAME = "channel-definition";
    public static final String CHANTYPE_TAG_NAME = "channel-type";
    public static final String DATABASE_TAG_NAME = "database";
    public static final String PROCESS_TAG_NAME = "processIf";

    // define ancestry paths in the XML that identify specific sub-tree
    // portions of the xml to be handled by different contentHandlers. This
    // enables us to only use portions of the tree or the whole tree as needed.
    
    public static final Path COMPONENT = Path.fromTag(COMPONENT_TAG_NAME);
    public static final Path WORKERS =
        new Path().append(COMPONENT_TAG_NAME).append(WORKER_TAG_NAME);
    public static final Path PROCESS =
        new Path().append(COMPONENT_TAG_NAME).append(PROCESS_TAG_NAME);
    public static final Path SERVICES =
        new Path().append(COMPONENT_TAG_NAME).append(SERVICE_TAG_NAME);
    public static final Path EXTENSIONS =
        new Path().append(COMPONENT_TAG_NAME).append(EXTENSION_TAG_NAME);
    public static final Path CHANDEFS =
        new Path().append(COMPONENT_TAG_NAME).append(CHANDEF_TAG_NAME);
    public static final Path CHANTYPES =
        new Path().append(COMPONENT_TAG_NAME).append(CHANTYPE_TAG_NAME);
    public static final Path DBDEFS =
        new Path().append(COMPONENT_TAG_NAME).append(DATABASE_TAG_NAME);
        
    /**
       Constructs a new CAR descriptor handler that will parse the descriptor
       passing relevant portions to handlers designed specifically for that
       portion.
    */
    DescriptorHandler(JarFile jarFile)
    {
        ParsingContext ctx = new ParsingContext(jarFile);

        PathRouter[] routers =
            new PathRouter[] {
                new PathRouter(WORKERS, new WorkerTagHandler(workers)),
                new PathRouter(SERVICES, services),
                new PathRouter(
                    DescriptorHandler.EXTENSIONS,
                    new ExtensionTagHandler(ctx)),
                new PathRouter(
                    DescriptorHandler.CHANDEFS,
                    new ChannelDefinitionTagHandler(ctx)),
                new PathRouter(
                    DescriptorHandler.CHANTYPES,
                    new ChannelTypeTagHandler(ctx)),
                new PathRouter(
                    DescriptorHandler.DBDEFS,
                    new DatabaseTagHandler(ctx))};
        parseDescriptor(jarFile, new ComponentTagHandler(ctx, routers));
    }

    /**
       Parse the deployment descriptor for a CAR. No need to synchronize since
       private and only called from constructor.
     */

    private void parseDescriptor(JarFile jarFile, ComponentTagHandler handler)
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
            log.error(
                            "The zip entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            " has an invalid format. Details: " + ze );
        }
        catch( IOException ioe )
        {
            log.error(
                            "Unable to read entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            ". Details: " + ioe );
        }
        catch( SecurityException se )
        {
            log.error(
                            "Unable to read entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            " because some entries are incorrectly signed. " +
                            "Details: " + se );
        }
        catch( FactoryConfigurationError fce )
        {
            log.error(
                            "Unable to read entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            " because a parser factory could not be created." +
                            " Details: " + fce );
        }
        catch( ParserConfigurationException pce )
        {
            log.error(
                            "Unable to read entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            " because a parser could not be created." +
                            " Details: " + pce );
        }
        catch( SAXException sxe )
        {
            log.error(
                            "Unable to read entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            " because a parser could not be created." +
                            " Details: " + sxe );
        }
        
        try
        {
            // can throw SAXException, IOException
            parser.parse( is, handler );
        }
        catch( RuntimeException re )
        {
            log.error(
                            "Unable to completely parse entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName() +
                            " because a fatal parser error occurred. ", re);
        }
        catch( IOException ioe )
        {
            log.error(
                            "Unable to completely parse entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName(), ioe );
        }
        catch( SAXException sxe )
        {
            log.error(
                            "Unable to completely parse entry " +
                            CarResources.DEPLOYMENT_DESCRIPTOR +
                            " in " + jarFile.getName(), sxe );
        }
        finally 
        {
            try 
            {
                if(is != null)
                is.close();
            } 
            catch (IOException ioe) {
                log.error(
                        "Unable to close inputStream", ioe);
            }
        }
    }

    /////// Methods for use by CarResources to aquire descriptor info.

    /**
       Adds to the passed in Properties object the names of workers and their
       implementing classes as specified in the parsed deployment descriptor.
     */
    public synchronized void getWorkers( Properties p )
    {
        if (workers.size() > 0)
        {
            for(Iterator itr = workers.entrySet().iterator(); itr.hasNext(); )
            {
                Map.Entry e = (Entry) itr.next();
                if ( ! p.containsKey(e.getKey()))
                    p.put(e.getKey(), e.getValue());
            }
        }
    }

    /**
       Generates event calls to the passed in handler for service descriptions
       extracted from the parsed the deployment descriptor.
     */
    public synchronized void getServices( ContentHandler c )
        throws SAXException
    {
        if ( ! services.isEmpty())
            services.outputBuffer(c);
    }
}


