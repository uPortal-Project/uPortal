/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.car;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles reading the extension tags in the descriptor. These tags
 * contain extension specific content that is foreign to the
 * deployment descriptor but understood by the handler class declared
 * by the extension tag. Upon entering an extension block the declared
 * handler class will be instantiated, the startDocument method will
 * be call indicating to the class that parsing of the content of its
 * extension block will be forthcoming. All events within the block
 * are then passed to the handler until the block is exited at which
 * point the endDocument method is called. Neither the startElement
 * nor the endElement events for the enclosing extension element are
 * passed to the handler class. It only sees events for content within
 * the block.
 * 
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
class ExtensionTagHandler
    extends DefaultHandler
{
    private static final Log log = LogFactory.getLog(ExtensionTagHandler.class);
    private ContentHandler extHandler = null;
    private ParsingContext ctx = null;

    public ExtensionTagHandler(ParsingContext ctx)
    {
        this.ctx = ctx;
    }
        
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
            
        if ( qName.equals( DescriptorHandler.EXTENSION_TAG_NAME ) &&
             ctx.getPath().equals( DescriptorHandler.EXTENSIONS ) ) 
        {
            // entering ext block so clean out old if around, make sure
            // that we haven't already processed this guy, and
            // get the handler for this block ready to roll
            extHandler = null;
                
            String handlerClass = atts.getValue( "contentHandler" );
        
            if ( handlerClass == null )
            {
                log.error(
                    "Ignoring invalid extension while "
                        + "processing deployment descriptor for "
                        + ctx.getJarFile().getName()
                        + ". Attribute 'contentHandler' must be "
                        + "specified.");
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
                log.error(
                    "Specified contentHandler class "
                        + handlerClass
                        + " not found. Ignoring extension block "
                        + "in deployment descriptor of "
                        + ctx.getJarFile().getName()
                        + ".");
                return;
            }
            /*
             * The following throwable check was added to prevent the 
             * CarResources from failing to load with a NoClassDefFound error
             * when a CAR was installed that was compile on a latter JVM than
             * the one running the system. The result is that an 
             * UnsupportedClassVersionError occurs which passes through the
             * checks for ClassNotFoundException and causes CarResources'
             * static initializer to fail resulting in a misleading
             * NoClassDefFoundError showing in the browser with no indication 
             * as to the real underlying cause.
             */
            catch( Throwable t)
            {
                log.error(
                        "Error occurred loading specified contentHandler class "
                            + handlerClass
                            + ". Ignoring extension block "
                            + "in deployment descriptor of "
                            + ctx.getJarFile().getName()
                            + ".", t);
                    return;
            }
            try
            {
                obj = c.newInstance();
            }
            catch( Exception e )
            {
                log.error(
                    "Unable to create specified "
                        + "contentHandler class "
                        + handlerClass
                        + " for extension block"
                        + " in deployment descriptor of "
                        + ctx.getJarFile().getName()
                        + ". Ignoring extension. Details: "
                        + e);
                return;
            }
            try
            {
                extHandler = (ContentHandler) obj;
            }
            catch( ClassCastException cce )
            {
                log.error(
                    "Specified contentHandler class "
                        + handlerClass
                        + " for extension in deployment "
                        + "descriptor of "
                        + ctx.getJarFile().getName()
                        + " does not implement ContentHandler. "
                        + "Ignoring extension.");
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
        if ( qName.equals( DescriptorHandler.EXTENSION_TAG_NAME ) &&
             ctx.getPath().equals( DescriptorHandler.EXTENSIONS ) ) 
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
 
