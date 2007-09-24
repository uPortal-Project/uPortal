/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

 /**
 * Filter incorporating channel attributes for the second
 * (theme) XSLT transformation.
 * @author Peter Kharchenko
 * @version $Revision$
 */

package org.jasig.portal;

import java.util.Enumeration;

import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

public class ThemeAttributesIncorporationFilter extends SAX2FilterImpl
{
    protected ThemeStylesheetUserPreferences tsup;

    // downward
    public ThemeAttributesIncorporationFilter(ContentHandler handler, ThemeStylesheetUserPreferences prefs) {
        super(handler);
        this.tsup=prefs;
    }

    // upward
    public ThemeAttributesIncorporationFilter(XMLReader parent, ThemeStylesheetUserPreferences prefs) {
        super(parent);
        this.tsup=prefs;
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        // recognizing "channel"
        if (qName.equals("channel")) {
            AttributesImpl attsImpl=new AttributesImpl(atts);
            String channelSubscribeId = attsImpl.getValue("ID");
            for(Enumeration ca=tsup.getChannelAttributeNames(); ca.hasMoreElements(); ) {
                String attrName=(String) ca.nextElement();
                attsImpl.addAttribute("",attrName,attrName,"CDATA",tsup.getChannelAttributeValue(channelSubscribeId,attrName));
            }
            super.startElement(uri,localName,qName,attsImpl);
        } else {
            super.startElement(uri,localName,qName, atts);
        }
    }
}

