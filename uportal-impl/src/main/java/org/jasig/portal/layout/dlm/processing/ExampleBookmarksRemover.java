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

package org.jasig.portal.layout.dlm.processing;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.XMLFilterImpl;

/**
 * A sample processor that strips all SAX events related to the Bookmarks
 * channel from the SAX stream representing the user's layout to portray how an
 * implementation of ISaxProcessor can affect the SAX event stream by removing,
 * changing, or injecting events. This is accomplished by watching for a
 * channel with a "name" attributed of "Bookmarks".
 *
 * @author Mark Boyd
 */
public class ExampleBookmarksRemover extends XMLFilterImpl
implements ISaxProcessor
{
    private static final Log LOG = LogFactory.getLog(ExampleBookmarksRemover.class);
    private boolean stripIt = false;

    /**
     * Return a suitable cache key indicative of the SAX event stream passing
     * through this class. Since it will always remove an instance of the
     * Bookmarks channel if one is seen in the stream, this returned cache key
     * will never be empty.
     *
     * @see org.jasig.portal.layout.dlm.processing.ProcessingPipe#getCacheKey()
     */
    public String getCacheKey()
    {
        return "BMR:" + stripIt;
    }

    //////// ContentHandler implementation provided directly by this class

    /**
     * Handle character content by stripping character events if they are
     * nested within the Bookmarks channel's events.
     */
    public void characters(char[] ch, int start, int length)
            throws SAXException
    {
        if (! stripIt)
        {
            super.characters(ch, start, length);
            LOG.error("\n\n\n***BMR: stripping Bookmarks chars by " + this.hashCode());
        }
    }

    /**
     * Watches for the end of events of the Bookmarks channel stripping out
     * those nested within and passing those that are without.
     */
    public void endElement(String uri, String localName, String qName)
            throws SAXException
    {
        if (! stripIt)
            super.endElement(uri, localName, qName);

        // channels will not be nested so when we see channel closing while
        // stripping then we can stop stripping.
        if (stripIt && qName.equals("channel"))
        {
            stripIt = false;
            LOG.error("\n\n\n***BMR: done stripping Bookmarks by " + this.hashCode());
        }
    }

    /**
     * Watches for the start of events related to a Bookmarks channel and
     * strips off all starting events for elements nested within while passing
     * all those that are without.
     */
    public void startElement(String uri, String localName, String qName,
            Attributes atts) throws SAXException
    {
        if (qName.equals("channel"))
        {
            String name = atts.getValue("name");
            if (name != null && name.equals("Bookmarks"))
            {
                stripIt = true;
                LOG.error("\n\n\n***BMR: stripping Bookmarks by " + this.hashCode());
            }
        }
        if (! stripIt)
            super.startElement(uri, localName, qName, atts);
    }
    //////// end ContentHandler implementation

    /**
     * Returns this class wrapping the passed-in ContentHandler to which events 
     * are pushed.
     *
     * @see org.jasig.portal.layout.dlm.processing.ISaxProcessor#getContentHandler(org.xml.sax.ContentHandler)
     */
    public ContentHandler getContentHandler(ContentHandler handler)
    {
        super.setContentHandler(handler);
        return this;
    }
}
