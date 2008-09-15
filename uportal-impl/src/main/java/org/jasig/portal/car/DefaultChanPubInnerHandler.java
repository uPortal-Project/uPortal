/* Copyright 2002 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.car;

import java.io.ByteArrayInputStream;

import org.jasig.portal.ChannelDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.tools.chanpub.ChannelPublisher;
import org.jasig.portal.tools.chanpub.IChannelPublisher;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Buffers a single channel definition located in a CAR
 * channel-definition block and delegates to the chanpub
 * ChannelPublisher to publish the channel.  Also strips our groups
 * and categories definitions since groups and categories can change
 * from site to site. If not specified then ChannelPublisher will
 * place in the "Auto-Published" category and grant access to
 * admins. Then admins can determine in what category it should be
 * placed and to whom it should be granted.
 * 
 * @author Mark Boyd  {@link <a href="mailto:mark.boyd@engineer.com">mark.boyd@engineer.com</a>}
 * @version $Revision$
 */
public class DefaultChanPubInnerHandler
    extends DefaultHandler
{
    private static final Log log = LogFactory.getLog(DefaultChanPubInnerHandler.class);
    private ParsingContext ctx = null;
    private StringBuffer buffer = new StringBuffer();
    private StringBuffer charBuf = new StringBuffer();

    public DefaultChanPubInnerHandler( ParsingContext ctx )
    {
        this.ctx = ctx;
        buffer.append("<!DOCTYPE channel-definition " +
                "SYSTEM \"channelDefinition.dtd\">");
    }
        
    public void startElement(String namespaceURI,
                             String localName,
                             String qName,
                             Attributes atts)
        throws SAXException
    {
        // channel definition elements currently have textual content or 
        // other elements and don't have attributes so don't cache attribs.
        buffer.append('<');
        buffer.append(qName);
        buffer.append('>');
        charBuf = new StringBuffer();
    }

    public void characters(char[] ch,
                           int start,
                           int length)
        throws SAXException
    {
        charBuf.append( ch, start, length );
    }
        
    public void endElement(String namespaceURI,
                           String localName,
                           String qName)
        throws SAXException
    {
        // capture the closing tag info
        buffer.append(charBuf.toString());
        buffer.append("</");
        buffer.append(qName);
        buffer.append('>');
        // clean out the buffer at end also to properly handle nested elements
        charBuf = new StringBuffer();

        // now see if this is the last piece of info for this chan-def
        if (qName.equals(DescriptorHandler.CHANDEF_TAG_NAME)
            && ctx.getPath().equals(
                DescriptorHandler.CHANDEFS))
        {
            // leaving ext block so push the channel definition into the 
            // channel publisher to be published.
            try
            {
                byte[] bytes = buffer.toString().getBytes();

                if (log.isInfoEnabled())
                    log.info(
                            "CAR channel definition '"
                            + buffer.toString()
                            + "' ready to publish.");

                final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                final IChannelPublisher publisher =
                    ChannelPublisher.getChannelArchiveInstance();

                ChannelDefinition chanDef = null;

                chanDef = publisher.publishChannel(is);

                if (chanDef != null && log.isInfoEnabled())
                    log.info(
                        " Successfully published channel "
                            + chanDef.getTitle()
                            + " with fname "
                            + chanDef.getFName());
            }
            catch (Exception e)
            {
                if (log.isInfoEnabled())
                    log.info(
                            "A problem occurred during auto publishing.",
                            e);
            }
        }
    }
}
