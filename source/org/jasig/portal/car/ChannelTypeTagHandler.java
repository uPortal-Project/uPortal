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
import org.jasig.portal.tools.RegisterChannelType;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handles the channel-type tags in CAR deployment descriptors enabling
 * adding of channel types from within CARs automatically.
 *
 * @author Mark Boyd <mark.boyd@engineer.com>
 * @version $Revision$
 */
public class ChannelTypeTagHandler extends DefaultHandler
{
    //  /////////////////// Content Handler Implementations //////////////////    

    private static final Log log = LogFactory.getLog(ChannelTypeTagHandler.class);
    private String clazz = null;
    private String name = null;
    private String description = null;
    private String uri = null;
    private ParsingContext ctx = null;
    private StringBuffer charBufr = new StringBuffer();

    /**
     * @param ctx
     */
    public ChannelTypeTagHandler(ParsingContext ctx)
    {
        this.ctx = ctx;
    }

    /**
       * Handle start element events.
       */
    public void startElement(
        String namespaceURI,
        String localName,
        String qName,
        Attributes atts)
        throws SAXException
    {
        // if starting a new channel type then clear out old values
        if (qName.equals(DescriptorHandler.CHANTYPE_TAG_NAME)
            && ctx.getPath().equals(DescriptorHandler.CHANTYPES))
        {
            this.clazz = null;
            this.name = null;
            this.description = null;
            this.uri = null;
            this.charBufr.delete(0, charBufr.length());
        }
    }

    /**
     * Handle the characters event to capture textual content for elements.
     */
    public void characters(char[] ch, int start, int length)
        throws SAXException
    {
        charBufr.append(ch, start, length);
    }

    /**
     * Handle the closing element event.
     */
    public void endElement(String namespaceURI, String localName, String qName)
        throws SAXException
    {
        if (qName.equals("name"))
            this.name = charBufr.toString();
        else if (qName.equals("class"))
            this.clazz = charBufr.toString();
        else if (qName.equals("description"))
            this.description = charBufr.toString();
        else if (qName.equals("uri"))
            this.uri = charBufr.toString();
        else if (qName.equals(DescriptorHandler.CHANTYPE_TAG_NAME)
            && ctx.getPath().equals(DescriptorHandler.CHANTYPES))
        {
            // leaving block so register the type
            try
            {
                RegisterChannelType.register(name, namespaceURI, localName, qName);
            }
            catch (Exception e)
            {
                log.error( 
                    "A problem occurred while registering a channel type. " +
                    e.getMessage(), e);
            }
        }
    }
}
