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
        if (log.isDebugEnabled())
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
        if (log.isDebugEnabled())
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
