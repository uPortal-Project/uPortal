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

    public AnchoringChannelIncorporationFilter(ContentHandler parent) {
        super(parent);
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
     * @return String representing the appropriate channel instance Id or null if none were found.
     */
    private String appendChannelName(String baseActionURL) {
        // parse the baseActionURL param and get the channel instance Id
        return getChannelInstanceId(baseActionURL);
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