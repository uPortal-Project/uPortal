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

import java.io.ByteArrayInputStream;

import org.jasig.portal.ChannelDefinition;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.tools.chanpub.ChannelPublisher;
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
 * @author Mark Boyd <mark.boyd@engineer.com>
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

                log.info(
                    "CAR channel definition '"
                        + buffer.toString()
                        + "' ready to publish.");

                final ByteArrayInputStream is = new ByteArrayInputStream(bytes);
                final ChannelPublisher publisher =
                    ChannelPublisher.getChannelArchiveInstance();

                ChannelDefinition chanDef = null;

                chanDef = publisher.publishChannel(is);

                if (chanDef != null)
                    log.info(
                        " Successfully published channel "
                            + chanDef.getTitle()
                            + " with fname "
                            + chanDef.getFName());
            }
            catch (Exception e)
            {
                log.info(
                    "A problem occurred during auto publishing.",
                    e);
            }
        }
    }
}
