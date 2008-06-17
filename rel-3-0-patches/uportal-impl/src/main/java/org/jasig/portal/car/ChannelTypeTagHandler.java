/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
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
        }
        this.charBufr.delete(0, charBufr.length());
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
                RegisterChannelType.register(clazz, name, description, uri);
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
