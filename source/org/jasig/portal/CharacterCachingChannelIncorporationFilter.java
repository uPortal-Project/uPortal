/**
 * Copyright © 2001 The JA-SIG Collaborative.  All rights reserved.
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

package org.jasig.portal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.jasig.portal.serialize.CachingSerializer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * A filter that incorporates channel content into the main SAX stream.
 * Unlike a regular {@link ChannelIncorporationFilter}, this class can
 * feed cache character buffers to the {@link CachingSerializer}.
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$
 */
public class CharacterCachingChannelIncorporationFilter extends SAX2FilterImpl {
    
    private static final Log log = LogFactory.getLog(CharacterCachingChannelIncorporationFilter.class);
    
    // keep track if we are "in" the <channel> element
    private boolean insideChannelElement = false;
    ChannelManager cm;

    // information about the current channel
    private String channelSubscribeId;
    private boolean ccaching;
    private CachingSerializer ser;

    Vector systemCCacheBlocks;
    Vector channelIdBlocks;

    // constructors

    // downward chaining
    public CharacterCachingChannelIncorporationFilter (ContentHandler handler, ChannelManager chanm, boolean ccaching)  {
        super(handler);

        if(handler instanceof CachingSerializer) {
            ser=(CachingSerializer) handler;
            this.ccaching=true;
        } else {
            this.ccaching=false;
        }

        this.cm = chanm;
        this.ccaching=(this.ccaching && ccaching);
        if(this.ccaching) {
            log.debug("CharacterCachingChannelIncorporationFilter() : ccaching=true");
            systemCCacheBlocks=new Vector();
            channelIdBlocks=new Vector();
        } else {
            log.debug("CharacterCachingChannelIncorporationFilter() : ccaching=false");
        }
    }


    /**
     * Obtain system character cache blocks.
     *
     * @return a <code>Vector</code> of system character blocks in between which channel renderings should be inserted.
     */
    public Vector getSystemCCacheBlocks() {
        if(ccaching) {
            return systemCCacheBlocks;
        } else {
            return null;
        }
    }

    /**
     * Obtain a vector of channels to be inserted into a current character cache.
     *
     * @return a <code>Vector</code> of cache entry blocks corresponding to channel 
     * subscribe Id(s) in an order in which they appear in the overall document.
     */
    public Vector getChannelIdBlocks() {
        if(ccaching) {
            return channelIdBlocks;
        } else {
            return null;
        }
    }

    public void startDocument () throws SAXException {
        if(ccaching) {
            // start caching
            try {
                if(!ser.startCaching()) {
                    log.error("CharacterCachingChannelIncorporationFilter::startDocument() : unable to start caching!");
                }
            } catch (IOException ioe) {
                log.error("CharacterCachingChannelIncorporationFilter::startDocument() : unable to start caching!");
            }
        }
        super.startDocument();
    }

    public void endDocument () throws SAXException {
        super.endDocument();
        if(ccaching) {
            // stop caching
            try {
                if(ser.stopCaching()) {
                    try {
                        systemCCacheBlocks.add(ser.getCache());
                    } catch (UnsupportedEncodingException e) {
                        log.error("CharacterCachingChannelIncorporationFilter::endDocument() : unable to obtain character cache, invalid encoding specified ! "+e);
                    } catch (IOException ioe) {
                        log.error("CharacterCachingChannelIncorporationFilter::endDocument() : IO exception occurred while retreiving character cache ! "+ioe);
                    }
                } else {
                    log.error("CharacterCachingChannelIncorporationFilter::endDocument() : unable to stop caching!");
                }
            } catch (IOException ioe) {
                log.error("CharacterCachingChannelIncorporationFilter::endDocument() : unable to stop caching! Exception: "+ioe.getMessage());
            }

        }
    }

    public void startElement (String uri, String localName, String qName, Attributes atts) throws SAXException  {
        if (!insideChannelElement) {
            // recognizing "channel"
            if (qName.equals ("channel")) {
                insideChannelElement = true;
                channelSubscribeId = atts.getValue("ID");
                if(ccaching) {
                    // save the old cache state
                    try {
                        if(ser.stopCaching()) {
                            //                            log.debug("CharacterCachingChannelIncorporationFilter::endElement() : obtained the following system character entry: \n"+ser.getCache());
                            systemCCacheBlocks.add(ser.getCache());
                        } else {
                            log.error("CharacterCachingChannelIncorporationFilter::startElement() : unable to reset cache state ! Serializer was not caching when it should've been !");
                        }
                    } catch (UnsupportedEncodingException e) {
                        log.error("CharacterCachingChannelIncorporationFilter::startElement() : unable to obtain character cache, invalid encoding specified ! "+e);
                    } catch (IOException ioe) {
                        log.error("CharacterCachingChannelIncorporationFilter::startElement() : IO exception occurred while retreiving character cache ! "+ioe);
                    }
                }
            } else {
                super.startElement(uri,localName,qName,atts);
            }
        }
    }

    public void endElement (String uri, String localName, String qName) throws SAXException  {
        if (insideChannelElement) {
            if (qName.equals ("channel")) {
                insideChannelElement = false;
                if (this.getContentHandler() != null) {
                    if(ccaching) {
                        channelIdBlocks.add(channelSubscribeId);
                    }
                    cm.outputChannel(channelSubscribeId,this.getContentHandler());
                    if(ccaching) {
                        // start caching again
                        try {
                            if(!ser.startCaching()) {
                                log.error("CharacterCachingChannelIncorporationFilter::endElement() : unable to restart cache after a channel end!");
                            }
                        } catch (IOException ioe) {
                            log.error("CharacterCachingChannelIncorporationFilter::endElement() : unable to start caching!");
                        }
                    }
                }
            }
            
        } else {
            super.endElement (uri,localName,qName);
        }
    }
}
