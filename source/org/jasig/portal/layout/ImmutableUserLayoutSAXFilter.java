/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal.layout;

import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This filter will mark all folders and channels as unremovable and immutable.
 * @author <a href="mailto:pkharchenko@interactivebusiness.com">Peter Kharchenko</a>
 * @version 1.0
 */
public class ImmutableUserLayoutSAXFilter extends SAX2FilterImpl {

    // downward
    public ImmutableUserLayoutSAXFilter(ContentHandler handler) {
        super(handler);
    }

    // upward
    public ImmutableUserLayoutSAXFilter(XMLReader parent) {
        super(parent);
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        // recognizing "channel"
        if (qName.equals("channel") || qName.equals("folder")) {
            // todo: deal with permissions on the top-level <layout> node
            AttributesImpl attsImpl=new AttributesImpl(atts);
            attsImpl.setValue(attsImpl.getIndex("unremovable"),"true");
            attsImpl.setValue(attsImpl.getIndex("immutable"),"true");
            super.startElement(uri,localName,qName,attsImpl);
        } else {
            super.startElement(uri,localName,qName, atts);
        }
    }
}

