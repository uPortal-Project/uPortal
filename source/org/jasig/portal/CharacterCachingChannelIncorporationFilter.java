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

import org.jasig.portal.utils.SAX2FilterImpl;
import org.jasig.portal.utils.SAX2BufferImpl;
import org.jasig.portal.services.LogService;
import org.xml.sax.*;
import org.xml.sax.helpers.*;
import org.xml.sax.ext.LexicalHandler;

import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import org.jasig.portal.serialize.*;

public class CharacterCachingChannelIncorporationFilter extends SAX2FilterImpl
{
    // keep track if we are "in" the <channel> element
    private boolean insideChannelElement = false;
    ChannelManager cm;

    // information about the current channel
    private Hashtable params;
    private String channelClassName;
    private String channelID;
    private String channelPublishID;
    private long timeOut;
    private boolean ccaching;
    private CachingSerializer ser;

    Vector systemCCacheBlocks;
    Vector channelIdBlocks;

    // constructors

    // downward chaining
    public CharacterCachingChannelIncorporationFilter (ContentHandler handler, ChannelManager chanm, boolean ccaching)
    {
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
            LogService.instance().log(LogService.DEBUG,"CharacterCachingChannelIncorporationFilter() : ccaching=true");
            systemCCacheBlocks=new Vector();
            channelIdBlocks=new Vector();
        } else {
            LogService.instance().log(LogService.DEBUG,"CharacterCachingChannelIncorporationFilter() : ccaching=false");
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
     * @return a <code>Vector</code> of channelID(s) in an order in which they appear in the overall document.
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
                    LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::startDocument() : unable to start caching!");
                }
            } catch (IOException ioe) {
                LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::startDocument() : unable to start caching!");
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
                        LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::endDocument() : unable to obtain character cache, invalid encoding specified ! "+e);
                    } catch (IOException ioe) {
                        LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::endDocument() : IO exception occurred while retreiving character cache ! "+ioe);
                    }
                } else {
                    LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::endDocument() : unable to stop caching!");
                }
            } catch (IOException ioe) {
                LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::endDocument() : unable to stop caching! Exception: "+ioe.getMessage());
            }

        }
    }

    public void startElement (String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        if (!insideChannelElement) {
            // recognizing "channel"
            if (qName.equals ("channel")) {
                insideChannelElement = true;

                // get class attribute
                channelClassName = atts.getValue("class");
                channelID = atts.getValue("ID");
                channelPublishID = atts.getValue("chanID");
                timeOut = java.lang.Long.parseLong (atts.getValue("timeout"));
                params = new Hashtable(0);
                if(ccaching) {
                    // save the old cache state
                    try {
                        if(ser.stopCaching()) {
                            //                            Logger.log(Logger.DEBUG,"CharacterCachingChannelIncorporationFilter::endElement() : obtained the following system character entry: \n"+ser.getCache());
                            systemCCacheBlocks.add(ser.getCache());
                        } else {
                            LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::startElement() : unable to reset cache state ! Serializer was not caching when it should've been !");
                        }
                    } catch (UnsupportedEncodingException e) {
                        LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::startElement() : unable to obtain character cache, invalid encoding specified ! "+e);
                    } catch (IOException ioe) {
                        LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::startElement() : IO exception occurred while retreiving character cache ! "+ioe);
                    }
                }
            } else {
                super.startElement(uri,localName,qName,atts);
            }
        } else if (qName.equals("parameter")) {
            params.put (atts.getValue("name"), atts.getValue("value"));
        }
    }

    public void endElement (String uri, String localName, String qName) throws SAXException
    {
        if (insideChannelElement) {
            if (qName.equals ("channel")) {
                insideChannelElement = false;
                if (this.getContentHandler() != null) {
                    if(ccaching) {
                        Vector chanEntry=new Vector(5);
                        chanEntry.add(this.channelID);
                        chanEntry.add(this.channelClassName);
                        chanEntry.add(new Long(timeOut));
                        chanEntry.add(this.params);
                        chanEntry.add(this.channelPublishID);
                        channelIdBlocks.add(chanEntry);
                        Object o=cm.getChannelCharacters (channelID, this.channelPublishID, this.channelClassName,this.timeOut,this.params);
                        if(o!=null) {
                            if(o instanceof String) {
                                LogService.instance().log(LogService.DEBUG,"CharacterCachingChannelIncorporationFilter::endElement() : received a character result for channelId=\""+channelID+"\"");
                                try {
                                    ser.printRawCharacters((String)o);
                                } catch (IOException ioe) {
                                    LogService.instance().log(LogService.DEBUG,"CharacterCachingChannelIncorporationFilter::endElement() : exception thrown while trying to output character cache for channelId=\""+channelID+"\". Message: "+ioe.getMessage());
                                }
                            } else if(o instanceof SAX2BufferImpl) {
                                LogService.instance().log(LogService.DEBUG,"CharacterCachingChannelIncorporationFilter::endElement() : received an XSLT result for channelId=\""+channelID+"\"");
                                // extract a character cache

                                // start new channel cache
                                try {
                                    if(!ser.startCaching()) {
                                        LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : unable to restart channel cache on a channel start!");
                                    }

                                    // output channel buffer
                                    SAX2BufferImpl buffer=(SAX2BufferImpl) o;
                                    buffer.setAllHandlers(this.contentHandler);
                                    buffer.outputBuffer();

                                    // save the old cache state
                                    if(ser.stopCaching()) {
                                        try {
                                            //                                            Logger.log(Logger.DEBUG,"CharacterCachingChannelIncorporationFilter::endElement() : obtained the following channel character entry: \n"+ser.getCache());
                                            cm.setChannelCharacterCache(channelID,ser.getCache());
                                        } catch (UnsupportedEncodingException e) {
                                            LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : unable to obtain character cache, invalid encoding specified ! "+e);
                                        } catch (IOException ioe) {
                                            LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : IO exception occurred while retreiving character cache ! "+ioe);
                                        }

                                    } else {
                                        LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : unable to reset cache state ! Serializer was not caching when it should've been !");
                                    }
                                } catch (IOException ioe) {
                                    LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : unable to start/stop caching!");
                                }
                            } else {
                                LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : ChannelManager.getChannelCharacters() returned an unidentified object!");
                            }

                            // start caching again
                            try {
                                if(!ser.startCaching()) {
                                    LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : unable to restart cache after a channel end!");
                                }
                            } catch (IOException ioe) {
                                LogService.instance().log(LogService.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : unable to start caching!");
                            }
                        }
                    } else {
                        cm.outputChannel (channelID, this.channelPublishID, this.getContentHandler (),this.channelClassName,this.timeOut,this.params);
                    }
                }
            }
        } else {
            super.endElement (uri,localName,qName);
        }
    }
}
