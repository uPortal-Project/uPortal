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

import java.lang.reflect.Constructor;
import org.jasig.portal.properties.PropertiesManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Processes all channel definitions located in a CAR and instantiates and 
 * delegates to an inner content handler for each block to do the real work
 * of publishing.
 * 
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
public class ChannelDefinitionTagHandler
    extends DefaultHandler
{
    private static final Log log = LogFactory.getLog(ChannelDefinitionTagHandler.class);
    private static Class cHandlerClass = null;
    private static Constructor cDefaultConstructor = null;
    private static Constructor cExtendedConstructor = null;
    
    private ContentHandler handlerInstance = null;
    private ParsingContext ctx = null;
    
    private static final String HANDLER_PROPERTY =
        "org.jasig.portal.car.ChannelDefinition.contentHandler";
        
    /**
     * Construct a ChannelDefinitionHandler that receives events from parsing
     * a channel archive deployment descriptor but only for any contained 
     * channel-definition elements and their children.
     * 
     * @param ctx
     */
    ChannelDefinitionTagHandler( ParsingContext ctx )
    {
        this.ctx = ctx;
        
        if ( cHandlerClass == null )
        {
            initialize();
        }
    }

    /**
     * Load an appropriate class for handling the channel definition content
     * and publishing the channel specified therein.
     */
    private void initialize()
    {
        String declaredClass = null;

        try
        {
            declaredClass = PropertiesManager.getProperty(HANDLER_PROPERTY);
        }
        catch(Exception e)
        {
            // no handler specified so use default.
        }
        if (declaredClass != null)
            loadTheClass(declaredClass);
        else
            cHandlerClass = DefaultChanPubInnerHandler.class;
            
        if (cHandlerClass != null)            
            getTheConstructor();
    }

    /**
     * Attempt to load the class specified.
     * @param handlerClass
     */
    private void loadTheClass( String handlerClass )
    {
        try
        {
            CarResources cRes = CarResources.getInstance();
            ClassLoader cl = cRes.getClassLoader();
            cHandlerClass = cl.loadClass(handlerClass);
        }
        catch (ClassNotFoundException clfe)
        {
            log.error(
                "Specified contentHandler class "
                    + handlerClass
                    + " specified in portal.properties not found. "
                    + "Ignoring channel-definition block "
                    + "in deployment descriptor of "
                    + ctx.getJarFile().getName()
                    + ".");
        }
    }

    private void getTheConstructor()
    {
        try
        {
            cExtendedConstructor =
                cHandlerClass.getConstructor(
                    new Class[] { ParsingContext.class });
            return;
        }
        catch(NoSuchMethodException nsme)
        {
            // since this constructor is optional ignore exception
            // and try default construction.
        }
        try
        {
            cDefaultConstructor =
                cHandlerClass.getConstructor( null );
        }
        catch(NoSuchMethodException nsme)
        {
            log.error(
                "Niether Extended constructor nor default, zero " +
                "parameter constructor were found " +
                "for specified contentHandler class "
                    + cHandlerClass.getName()
                    + " specified in portal.properties. Ignoring "
                    + "channel-definition block "
                    + "in deployment descriptor of "
                    + ctx.getJarFile().getName()
                    + ".", nsme);
        }
    }
    /**
     * Attempt to load an instance of the class. The classes must support a
     * default constructor but can optionally provide a constructor with a 
     * single argument of type DescriptorHandler. If found that constructor
     * will be used. If not found then the default constructor wil be used.
     * 
     * @return
     */
    private Object instantiateTheClass()
    {
        Object obj = null;
        
        try
        {
            if (cExtendedConstructor != null)
                obj = cExtendedConstructor.newInstance(new Object[] { ctx });
            else if (cDefaultConstructor != null )
                obj = cDefaultConstructor.newInstance(new Object[]{});
        }
        catch (Exception e)
        {
            log.error(
                "Unable to create specified contentHandler class "
                    + cHandlerClass.getName()
                    + " specified in portal.properties. Ignoring "
                    + "channel-definition block "
                    + "in deployment descriptor of "
                    + ctx.getJarFile().getName()
                    + ".", e);
        }
        return obj;
    }

    /**
     * Casts the object to a ContentHandler and logs any error that occurs.
     * 
     * @param obj
     * @return
     */
    private ContentHandler castToContentHandler( Object obj )
    {
        ContentHandler handler = null;
        
        try
        {
            handler = (ContentHandler) obj;
        }
        catch (ClassCastException cce)
        {
            log.error(
                "ContentHandler class "
                    + obj.getClass().getName()
                    + " specified in portal.properties"
                    + " does not implement ContentHandler."
                    + " Ignoring channel-definition block in"
                    + " deployment descriptor of "
                    + ctx.getJarFile().getName()
                    + ".");
        }
        return handler;
    }

///////////////////// Content Handler Implementations //////////////////    

    /**
     * Handle start element events.
     */
    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
        throws SAXException
    {
        // if starting a new channel definition then instantiate a new
        // inner handler to which to pass events
        if (qName.equals(DescriptorHandler.CHANDEF_TAG_NAME)
            && ctx.getPath().equals(
                DescriptorHandler.CHANDEFS))
        {
            if (cHandlerClass != null)
            {
                Object obj = instantiateTheClass();
                handlerInstance = castToContentHandler(obj);
            }
        }

        if ( handlerInstance != null )
            handlerInstance.startElement( namespaceURI, localName, qName, atts);
    }
    
    /**
     * Handle the characters event to capture textual content for elements.
     */
    public void characters(char[] ch,
                           int start,
                           int length)
        throws SAXException
    {
        if ( handlerInstance != null )
            handlerInstance.characters( ch, start, length );
    }

    /**
     * Handle the closing element event.
     */
    public void endElement(String namespaceURI,
                           String localName,
                           String qName)
        throws SAXException
    {
        // while within channel-definition block handler will be non-null and 
        // should receive all events.
        if ( handlerInstance != null )
            handlerInstance.endElement( namespaceURI, localName, qName );

        if (qName.equals(DescriptorHandler.CHANDEF_TAG_NAME)
            && ctx.getPath().equals(
                DescriptorHandler.CHANDEFS))
        {
            // leaving block so remove the handler in prep for a new
            // block
            handlerInstance = null;
        }
    }
}
