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

import org.xml.sax.*;
import org.xml.sax.helpers.*;
import java.io.*;
import java.util.Hashtable;
import java.util.Enumeration;
import java.util.Map;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.jsp.*;
import javax.servlet.http.*;

import org.apache.xml.serialize.*;

public class CharacterCachingChannelIncorporationFilter extends SAXFilterImpl
{
    // keep track if we are "in" the <channel> element
    private boolean insideChannelElement = false;
    ChannelManager cm;
    
    // information about the current channel
    private Hashtable params;
    private String channelClassName;
    private String channelID;
    private long timeOut;
    private boolean ccaching;
    private CachingSerializer ser;
    
    Vector systemCCacheBlocks;
    Vector channelIdBlocks;

    public CharacterCachingChannelIncorporationFilter (DocumentHandler handler, ChannelManager chanm, boolean ccaching)
    {
        super (handler);

        if(handler instanceof CachingSerializer) {
            ser=(CachingSerializer) handler;
            this.ccaching=true;
        } else {
            this.ccaching=false;
        }

        this.cm = chanm;
        this.ccaching=(this.ccaching && ccaching);
        if(this.ccaching) {
            Logger.log(Logger.DEBUG,"CharacterCachingChannelIncorporationFilter() : ccaching=true");
            systemCCacheBlocks=new Vector();
            channelIdBlocks=new Vector();
        } else {
            Logger.log(Logger.DEBUG,"CharacterCachingChannelIncorporationFilter() : ccaching=false");
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
                    Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::startDocument() : unable to start caching!");
                }
            } catch (IOException ioe) {
                Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::startDocument() : unable to start caching!");
            }
        }
        super.startDocument();
    }

    public void endDocument () throws SAXException {
        super.endDocument();
        if(ccaching) {
            // stop caching
            if(ser.stopCaching()) {
                try {
                    systemCCacheBlocks.add(ser.getCache());
                } catch (UnsupportedEncodingException e) {
                    Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::endDocument() : unable to obtain character cache, invalid encoding specified ! "+e);
                } catch (IOException ioe) {
                    Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::endDocument() : IO exception occurred while retreiving character cache ! "+ioe);
                }
            } else {
                Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::endDocument() : unable to stop caching!");            }
            
        }
    }

    public void startElement (java.lang.String name, org.xml.sax.AttributeList atts) throws SAXException
    {
        if (!insideChannelElement) {
            // recognizing "channel"
            if (name.equals ("channel")) {
                insideChannelElement = true;

                // get class attribute
                channelClassName = atts.getValue ("class");
                channelID = atts.getValue ("ID");
                timeOut = java.lang.Long.parseLong (atts.getValue ("timeout"));
                params = new Hashtable (0);
                if(ccaching) {
                    // save the old cache state
                    if(ser.stopCaching()) {
                        try {
                            systemCCacheBlocks.add(ser.getCache());
                        } catch (UnsupportedEncodingException e) {
                            Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::startElement() : unable to obtain character cache, invalid encoding specified ! "+e);
                        } catch (IOException ioe) {
                                        Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::startElement() : IO exception occurred while retreiving character cache ! "+ioe);
                        }
                            
                    } else {
                        Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::startElement() : unable to reset cache state ! Serializer was not caching when it should've been !");
                    }

                }
            } else {
                super.startElement (name,atts);
            }
        } else if (name.equals ("parameter")) {
            params.put (atts.getValue ("name"), atts.getValue ("value"));
        }
    }

    public void endElement (java.lang.String name) throws SAXException
    {
        if (insideChannelElement) {
            if (name.equals ("channel")) {
                insideChannelElement = false;
                if (super.outDocumentHandler != null) {
                    if(ccaching) {
                        Vector chanEntry=new Vector(4);
                        chanEntry.add(this.channelID); 
                        chanEntry.add(this.channelClassName); 
                        chanEntry.add(new Long(timeOut));
                        chanEntry.add(this.params); 
                        channelIdBlocks.add(chanEntry);
                        Object o=cm.getChannelCharacters (channelID, this.channelClassName,this.timeOut,this.params);
                        if(o!=null) {
                            if(o instanceof String) {
                                Logger.log(Logger.DEBUG,"CharacterCachingChannelIncorporationFilter::endElement() : received a character result for channelId=\""+channelID+"\"");
                                ser.printRawCharacters((String)o);
                            } else if(o instanceof SAXBufferImpl) {
                                Logger.log(Logger.DEBUG,"CharacterCachingChannelIncorporationFilter::endElement() : received an XSLT result for channelId=\""+channelID+"\"");
                                // extract a character cache 

                                // start new channel cache
                                try {
                                    if(!ser.startCaching()) {
                                        Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : unable to restart channel cache on a channel start!");
                                    }
                                    
                                    // output channel buffer
                                    ((SAXBufferImpl)o).outputBuffer(this.getDocumentHandler());
                                    
                                    // save the old cache state
                                    if(ser.stopCaching()) {
                                        try {
                                            cm.setChannelCharacterCache(channelID,ser.getCache());
                                        } catch (UnsupportedEncodingException e) {
                                            Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : unable to obtain character cache, invalid encoding specified ! "+e);
                                        } catch (IOException ioe) {
                                            Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : IO exception occurred while retreiving character cache ! "+ioe);
                                        }

                                    } else {
                                        Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : unable to reset cache state ! Serializer was not caching when it should've been !");
                                    }
                                } catch (IOException ioe) {
                                    Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : unable to start/stop caching!");
                                }
                            } else {
                                Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : ChannelManager.getChannelCharacters() returned an unidentified object!");
                            }

                            // start caching again
                            try { 
                                if(!ser.startCaching()) {
                                    Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : unable to restart cache after a channel end!");
                                }
                            } catch (IOException ioe) {
                                Logger.log(Logger.ERROR,"CharacterCachingChannelIncorporationFilter::endElement() : unable to start caching!");
                            }
                        }
                    } else {
                        cm.outputChannel (channelID, this.getDocumentHandler (),this.channelClassName,this.timeOut,this.params);
                    }
                }
            }
        } else { 
            super.endElement (name);
        }
    }
}
