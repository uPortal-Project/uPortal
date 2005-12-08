/* Copyright 2001, 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Vector;

import org.jasig.portal.serialize.CachingSerializer;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;


/**
 * A filter that incorporates channel content into the main SAX stream.
 * Unlike a regular {@link ChannelIncorporationFilter}, this class can
 * feed cache character buffers to the {@link CachingSerializer}.
 * 
 * <p>Replaces
 * &lt;channel ID="channelSubcribeId"/&gt;
 * elements with channel output from the IChannelRenderer for that 
 * channelSubscribeId.</p>
 * 
 * <p><Replaces 
 * &lt;channel-title 
 *     channelSubcribeId="channelSubcribeId" 
 *     defaultValue="defaultTitle" /&gt;
 *
 * elements with dynamic channel title from the IChannelRenderer for that 
 * channelSubcribeId, or the provided defaultValue in the case where there is 
 * no dynamic channel title.</p>
 *
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version $Revision$ $Date$
 */
public class CharacterCachingChannelIncorporationFilter extends SAX2FilterImpl {
    
    /** 
     * Track what, if any, incorporation element we are currently processing.
     * "channel" if we are "in" the <channel> element.
     * "channel-title" if we are "in" the <channel-title> element.
     * Null if we are not in one of these elements.  Other elements are not
     * processed by this filter (are not "incorporation elements").
     */
    private String insideElement = null;
    
    ChannelManager cm;

    /**
     * ChannelSubscribeId of the currently-being-incorporated channel, if any.
     * Null if not currently incorporating a channel (not in an incorporation
     * element.)
     */ 
    private String channelSubscribeId = null;
    
    private String channelTitle = null;
    
    private boolean ccaching;
    private CachingSerializer ser;

    Vector systemCCacheBlocks;
    Vector channelIdBlocks;

    // constructors

    /**
     * Downward chaining constructor.
     */
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
                    log.error("CharacterCachingChannelIncorporationFilter::startDocument() " +
                    		": unable to start caching!");
                }
            } catch (IOException ioe) {
                log.error("CharacterCachingChannelIncorporationFilter::startDocument() " +
                		": unable to start caching!",ioe);
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
                        log.error("CharacterCachingChannelIncorporationFilter::endDocument() " +
                        		": unable to obtain character cache, invalid encoding specified ! ",e);
                    } catch (IOException ioe) {
                        log.error("CharacterCachingChannelIncorporationFilter::endDocument() " +
                        		": IO exception occurred while retreiving character cache ! ",ioe);
                    }
                } else {
                    log.error("CharacterCachingChannelIncorporationFilter::endDocument() " +
                    		": unable to stop caching!");
                }
            } catch (IOException ioe) {
                log.error("CharacterCachingChannelIncorporationFilter::endDocument() " +
                		": unable to stop caching!", ioe);
            }

        }
    }

    public void startElement(String uri, String localName, String qName,
			Attributes atts) throws SAXException {

		if (log.isTraceEnabled()) {
			log
					.trace("CharacterCachingChannelIncorporationFilter is filtering element with "
							+ "uri="
							+ uri
							+ " localName="
							+ localName
							+ " qName="
							+ qName
							+ "atts="
							+ atts
							+ " .  Current channelSubscribeId="
							+ this.channelSubscribeId
							+ " and in element "
							+ this.insideElement);
		}

		if (!isInIncorporationElement()) {
			// recognizing "channel"
			if (qName.equals("channel")) {
				this.insideElement = "channel";
				this.channelSubscribeId = atts.getValue("ID");
                if (this.channelSubscribeId == null) // fname access used
                {
                    String fname = atts.getValue("fname");
                    if (fname.equals("")) // can't obtain subscribe id
                        log.error("Incurred a channel with no subscribe id " +
                                "in attribute 'ID' and no functional name " +
                                "in attribute 'fname'.");
                    else
                    {
                        // get the channel from layout if there or instantiate 
                        // in transient layout manager if not
                        try
                        {
                            this.channelSubscribeId = cm.getSubscribeId(fname);
                        } catch (PortalException e)
                        {
                            log.error("Unable to obtain subscribe id for " +
                                    "channel with functional name '" + fname +
                                "'.", e);
                        }
                    }
                }
				if (ccaching) {
					// save the old cache state
					try {
						if (ser.stopCaching()) {
							if (log.isDebugEnabled()) {
								log
										.debug("CharacterCachingChannelIncorporationFilter::endElement() "
												+ ": obtained the following system character entry: \n"
												+ ser.getCache());
							}
							systemCCacheBlocks.add(ser.getCache());
						} else {
							log
									.error("CharacterCachingChannelIncorporationFilter::startElement() "
											+ ": unable to reset cache state ! Serializer was not caching when it should've been !");
						}
					} catch (UnsupportedEncodingException e) {
						log
								.error(
										"CharacterCachingChannelIncorporationFilter::startElement() "
												+ ": unable to obtain character cache, invalid encoding specified ! ",
										e);
					} catch (IOException ioe) {
						log
								.error(
										"CharacterCachingChannelIncorporationFilter::startElement() "
												+ ": IO exception occurred while retreiving character cache ! ",
										ioe);
					}
				}
			} else if (qName.equals("channel-title")) {
				this.insideElement = "channel-title";
				this.channelSubscribeId = atts.getValue("channelSubscribeId");
				this.channelTitle = atts.getValue("defaultValue");
			} else {
				// not in an incorporation element and not starting one this class
				// handles specially, so pass the element through this filter.
				super.startElement(uri, localName, qName, atts);

			}
		} else {
			// inside an incorporation element, do nothing.
			if (log.isTraceEnabled()) {
				log.trace("Ignoring element " + qName);
			}
		}
	}

    public void endElement (String uri, String localName, String qName) throws SAXException  {
    	
    	if (log.isTraceEnabled()) {
    		log.trace("CharacterCachingChannelIncorporationFilter is filtering end element with "
    				+ "url=" + uri + " localName=" + localName 
					+ " qName=" + qName + " .  Current channelSubscribeId=" 
					+ this.channelSubscribeId + " and in element " + this.insideElement);
    	}
    	
        // if inside an element this filter handles (incorporates content in place of)
    	// then this endElement call may be the time to end one of these special elements.
    	if (isInIncorporationElement()) {
            if (qName.equals ("channel") && this.insideElement.equals("channel")) {

            	try {
            	    ContentHandler contentHandler = getContentHandler();
					if (contentHandler != null) {
						if (ccaching) {
							channelIdBlocks.add(channelSubscribeId);
						}
						cm.outputChannel(channelSubscribeId, contentHandler);
						if (ccaching) {
							// start caching again
							try {
								if (!ser.startCaching()) {
									log
											.error("CharacterCachingChannelIncorporationFilter::endElement() : unable to restart cache after a channel end!");
								}
							} catch (IOException ioe) {
								log
										.error(
												"CharacterCachingChannelIncorporationFilter::endElement() : unable to start caching!",
												ioe);
							}
						}
					} else {
						// contentHandler was null. This is a serious problem,
						// since
						// filtering is pointless if it's not writing back to a
						// contentHandler
						log
								.error("null ContentHandler prevents outputting channel with subscribe id = "
										+ channelSubscribeId);
					}
            	} finally {
                    endIncorporationElement();
            	}
            } else if (qName.equals("channel-title") && this.insideElement.equals("channel-title")) {
            	
    			if (log.isDebugEnabled()) {
            		log.debug("Incorporating title for channel with subscribeId=[" + this.channelSubscribeId + "]");
            	}
            	
            	try {
            		
            		ContentHandler contentHandler = getContentHandler();
            		if (contentHandler != null) {
						String dynamicChannelTitle = cm
								.getChannelTitle(channelSubscribeId);

						// if there is a dynamic channel title, use that.
						// otherwise, stick with the title that was read from
						// the channel-title element we're replacing.
						if (dynamicChannelTitle != null) {
							this.channelTitle = dynamicChannelTitle;
						}
						
						AttributesImpl noAttributes = new AttributesImpl();
						
						//contentHandler.startElement("", "", "span", noAttributes);
						
						char[] channelTitleArray = this.channelTitle.toCharArray();
						
						contentHandler.characters(channelTitleArray, 0, channelTitleArray.length);
						//contentHandler.endElement("", "", "span");

					} else {
            			// contentHandler was null. This is a serious problem,
            			// since filtering is pointless if it's not writing back
            			// to a contentHandler.
            			log.error("null ContentHandler prevents outputting channel title with subcribe id = " + this.channelSubscribeId);
            		}
            		
            	} finally {
                    endIncorporationElement();
            	}

            }
            
        } else {
        	// pass non-incorporation elements through untouched.
            super.endElement (uri,localName,qName);
        }
    }
    
    public String toString() {
    	StringBuffer sb = new StringBuffer();
    	sb.append(getClass());
    	sb.append(" caching: ").append(this.ccaching);
    	sb.append(" currently processing: subscribeId=").append(this.channelSubscribeId);
    	sb.append(" in incorporation element: ").append(this.insideElement);
    	
    	return sb.toString();
    }
    
    /**
     * Returns true if this filter is currently processing an incorporation element.
     * The purpose of this method is to allow ignoring of elements starting within elements 
     * this filter is already incorporating, and allow ignoring of end elements except when
     * those end elements end incorporation elements.
     * 
     * @return true if in an incorporation element, false otherwise
     */
    private boolean isInIncorporationElement() {
    	// in an incorporation element when the stored name of that element is not null.
    	return this.insideElement != null;
    }
    
    /**
     * Reset filter state to not being "inside" an incorporation element (and
     * therefore instead being available to process the next incorporation 
     * element encountered).  
     *
     * @throws IllegalStateException if not currently inside an element.
     */
    private void endIncorporationElement() {
    	if (this.channelSubscribeId == null || this.insideElement == null) {
    		throw new IllegalStateException("Cannot end element when not in element:" + this);
    	}
    	this.channelSubscribeId = null;
    	this.insideElement = null;
    }
    
    
    
}

