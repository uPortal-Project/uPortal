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

import org.apache.xpath.XPathAPI;
import org.jasig.portal.utils.SAX2FilterImpl;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
* This is a Filter that will intercept the SAX events and modify certain attributes with the 
* appropriate anchors to render the channel appropriately
* 
* @author <a href="mailto:flopez@unicon.net">Freddy Lopez</a>
* @version $Revision$
*/

public class AnchoringChannelIncorporationFilter extends SAX2FilterImpl {

    private static final String PORTAL_PREFIX = ".render.";
    private static final String ANCHOR_CHAR = "#";
    private static final String HREF_KEY = "href";
    private static final String FORM_KEY = "form";
    private static final String ANCHOR_LINK_KEY = "a";
    private static final String ACTION_KEY = "action";
    private Document userLayout;

    public AnchoringChannelIncorporationFilter(Document userLayout,
            ContentHandler parent) {
        super(parent);
        this.userLayout = userLayout;
    }

    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException {

        AttributesImpl newAtts = null;
        // looking for an <a> or <form> tag element
        if (qName.equalsIgnoreCase(ANCHOR_LINK_KEY) || qName.equalsIgnoreCase(FORM_KEY)) {
            // found an <a> or <form>, let's peek at the attributes it contains
            if (atts != null) {
                for (int i = 0; i < atts.getLength(); i++) {
                    // does it contain either an "href" or "action" attribute
                    if (atts.getQName(i).toLowerCase().equalsIgnoreCase(
                            HREF_KEY) || atts.getQName(i).toLowerCase().equalsIgnoreCase(ACTION_KEY)) {
                        // found the attribute, now lets make sure it points back to a channel
                        String value = atts.getValue(i);
                        if (value.indexOf(PORTAL_PREFIX) != -1) {
                            // this link points back to a channel, so let's
                            // rewrite it and place back into the Attribute Object
                            newAtts = new AttributesImpl(atts);
                            String anchorName = appendChannelName(value);
                            if (anchorName != null) {
                                value += ANCHOR_CHAR + anchorName;
                            }
                            
                            newAtts.setValue(i, value);
                        }
                    }
                }
            }
        }
        if (newAtts == null)
            super.startElement(uri, localName, qName, atts);
        else
            super.startElement(uri, localName, qName, newAtts);
    }

    /**
     * 
     * This method will parse the baseActionURL parameter and pull out the
     * channel instance Id and return the appropriate channel name.
     * 
     * @param baseActionURL that will be parsed to find the correct channel instance Id
     * @return String representing the appropriate channel "title" name used when it was published
     */
    private String appendChannelName(String baseActionURL) {
        // parse the baseActionURL param and get the channel instance Id
        String channelInstanceId = getChannelInstanceId(baseActionURL);
        // lookup the channel name based on this channel instance Id
        return getChannelName(channelInstanceId);
    }

    /**
     * 
     * Will return the appropriate channel name based on the incoming channel
     * instance Id.
     * 
     * @param channelInstanceId
     * @return String representing the appropriate channel "title" name used when it was published 
     */
    private String getChannelName(String channelInstanceId) {
        try {
            Document userLayout = getUserLayout();
            Node channelNode = userLayout.getElementById(channelInstanceId);
            if (channelNode == null) {
              // Aggregated Layout Managers has a different DOM implementation  
              channelNode = XPathAPI.selectSingleNode(userLayout, "//*[@ID='"+channelInstanceId+"']");
            }
            
            if (channelNode == null) return null;
            // else we found the channel XML, let's find the name
            String channelName = ((Element) channelNode).getAttribute("title");
            if (channelName != null) return channelName;
        } catch (Exception e) {
            // if we threw, let's give up on this anchor link 
        }
        return null;
    }

    private Document getUserLayout() throws Exception {
        return userLayout;
    }

    /**
     * 
     * This method will parse the baseActionURL and return the channel instance
     * Id.
     * 
     * @param baseActionURL
     * @return String representing the channel instance Id
     */
    private String getChannelInstanceId(String baseActionURL) {
        int foundInstanceAt = baseActionURL.indexOf(".target.");
        int foundUpAt = baseActionURL.indexOf(".uP");
        if (foundInstanceAt != -1 && foundUpAt != -1)
          return baseActionURL
                .substring((foundInstanceAt + 8), foundUpAt);

        return null;
    }
}