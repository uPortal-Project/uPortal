/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.car;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles routing events to a registered set of handlers that each
 * handles different portions of the descriptors xml tree. For
 * example, a services block and all events for its top level services
 * tag and any nested elements would be routed to the handler for
 * services. This object also keeps track of the full traversal path
 * from an element back to the containing document object.
 * 
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
public class RoutingHandler
    extends DefaultHandler
{
    private ParsingContext ctx = null;
    private PathRouter currentRouter = null;
    private PathRouter[] routers = null;

    RoutingHandler(ParsingContext ctx, PathRouter[] routers)
    {
        this.routers = routers;
        this.ctx = ctx;
    }

    /////// ContentHandler methods of interest

    public void startElement(java.lang.String namespaceURI,
                         java.lang.String localName,
                         java.lang.String qName,
                         Attributes atts)
        throws SAXException
    {
        // add to the path as we traverse the xml tree.
        ctx.getPath().append( qName );

        // if no one is handling this portion of the sub tree see if anyone.
        // should.
        if ( currentRouter == null )
            for( int i=0; currentRouter == null && i<routers.length; i++ )
                if ( routers[i].looksFor( ctx.getPath() ) )
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
            if ( currentRouter.looksFor( ctx.getPath() ) )
                currentRouter = null;
        }

        // drop the last item from the list. should be same as localName.
        ctx.getPath().removeLast();
    }

    public void characters(char[] ch,
                       int start,
                       int length)
        throws SAXException
    {
        if ( currentRouter != null )
            currentRouter.handler.characters( ch, start, length );
    }
}
