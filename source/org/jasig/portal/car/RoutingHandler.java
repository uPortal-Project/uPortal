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
