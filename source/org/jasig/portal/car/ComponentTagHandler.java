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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * The top level SAX event handler that receives event from the SAX parser 
 * when parsing a deployment descriptor. The top level element in a deployment
 * descriptor can contain any number of children all of which can conditionally
 * be wrapped in a processIf tag allowing for conditional parsing of one to many
 * elements. This handler performs the switching between a filtering handler
 * and a handler that routes events to the appropriate handlers of specific
 * subsection of the descriptor tree.
 * 
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
public class ComponentTagHandler
extends DefaultHandler
{
    private static final Log log = LogFactory.getLog(ComponentTagHandler.class);
    private ParsingContext ctx = null;
    private RoutingHandler routingHandler = null;
    private ProcessIfTagHandler processTagHandler = null;
    
    private ContentHandler currentHandler = null;

    ComponentTagHandler(ParsingContext ctx, PathRouter[] routers)
    {
        routingHandler = new RoutingHandler(ctx, routers);
        processTagHandler = new ProcessIfTagHandler(ctx, routingHandler);
        currentHandler = routingHandler;        
        this.ctx = ctx;
    }
    
    /////// ErrorHandler interface methods

    public void error(SAXParseException exception)
        throws SAXException
    {
        log.debug(
            "A non-fatal parsing error occurred while parsing "
                + CarResources.DEPLOYMENT_DESCRIPTOR
                + " in "
                + ctx.getJarFile().getName()
                + "."
                , exception);
    }
    
    public void fatalError(SAXParseException exception)
        throws SAXException
    {
        throw new RuntimeException( exception.toString() );
    }

    public void warning(SAXParseException exception)
        throws SAXException
    {
        log.debug(
            "A parsing warning occurred while parsing "
                + CarResources.DEPLOYMENT_DESCRIPTOR
                + " in "
                + ctx.getJarFile().getName()
                + "."
                , exception);
    }

    /////// ContentHandler methods of interest

    public void startElement(java.lang.String namespaceURI,
                         java.lang.String localName,
                         java.lang.String qName,
                         Attributes atts)
        throws SAXException
    {
        // see if we are entering a process tag and if so wrap the routing with
        // a process handler for conditional filtering
        if (qName.equals(DescriptorHandler.PROCESS_TAG_NAME) &&
            ctx.getPath().equals(DescriptorHandler.COMPONENT))
        {
            processTagHandler.setAttributes(atts);
            currentHandler=processTagHandler;
        }
        else
            currentHandler.startElement(namespaceURI, localName, qName, atts);
    }
    
    public void endElement(java.lang.String namespaceURI,
                       java.lang.String localName,
                       java.lang.String qName)
                throws SAXException
    {
        // see if we are leaving a process tag and if so switch back to routing
        // since the process tag does not contribute to the traversal path the
        // path should be just the outermost component element
        if (qName.equals(DescriptorHandler.PROCESS_TAG_NAME) &&
            ctx.getPath().equals(DescriptorHandler.COMPONENT))
        {
            currentHandler=routingHandler;
        }
        else
            currentHandler.endElement(namespaceURI, localName, qName);
    }

    public void characters(char[] ch,
                       int start,
                       int length)
        throws SAXException
    {
        currentHandler.characters( ch, start, length );
    }
}
