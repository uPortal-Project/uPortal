/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
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
