/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
                log.error("CharacterCachingChannelIncorporationFilter::startDocument() : unable to start caching!",ioe);
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
                        log.error("CharacterCachingChannelIncorporationFilter::endDocument() : unable to obtain character cache, invalid encoding specified ! ",e);
                    } catch (IOException ioe) {
                        log.error("CharacterCachingChannelIncorporationFilter::endDocument() : IO exception occurred while retreiving character cache ! ",ioe);
                    }
                } else {
                    log.error("CharacterCachingChannelIncorporationFilter::endDocument() : unable to stop caching!");
                }
            } catch (IOException ioe) {
                log.error("CharacterCachingChannelIncorporationFilter::endDocument() : unable to stop caching!", ioe);
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
                        log.error("CharacterCachingChannelIncorporationFilter::startElement() : unable to obtain character cache, invalid encoding specified ! ",e);
                    } catch (IOException ioe) {
                        log.error("CharacterCachingChannelIncorporationFilter::startElement() : IO exception occurred while retreiving character cache ! ",ioe);
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
                            log.error("CharacterCachingChannelIncorporationFilter::endElement() : unable to start caching!",ioe);
                        }
                    }
                }
            }
            
        } else {
            super.endElement (uri,localName,qName);
        }
    }
}
