/* Copyright 2001 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/

package org.jasig.portal;

import java.util.Enumeration;

import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * Filter incorporating channel and folder attributes for the structure transformation
 * @author Peter Kharchenko
 * @version $Revision$
 */

public class StructureAttributesIncorporationFilter extends SAX2FilterImpl
{
    protected StructureStylesheetUserPreferences fsup;

    // downward
    public StructureAttributesIncorporationFilter(ContentHandler handler, StructureStylesheetUserPreferences prefs) {
        super(handler);
        this.fsup=prefs;
    }

    // upward
    public StructureAttributesIncorporationFilter(XMLReader parent, StructureStylesheetUserPreferences prefs) {
        super(parent);
        this.fsup=prefs;
    }

    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException
    {
        // recognizing "channel"
        if (qName.equals("channel")) {
            AttributesImpl attsImpl=new AttributesImpl(atts);
            String channelSubscribeId = attsImpl.getValue("ID");
            for(Enumeration ca=fsup.getChannelAttributeNames(); ca.hasMoreElements(); ) {
                String attrName=(String) ca.nextElement();
                attsImpl.addAttribute("",attrName,attrName,"CDATA",fsup.getChannelAttributeValue(channelSubscribeId,attrName));
                //		log.debug("adding attribute to channel="+channelID+" "+attrName+"="+fsup.getChannelAttributeValue(channelID,attrName));
            }
            super.startElement(uri,localName,qName,attsImpl);
        } else 	if (qName.equals("folder")) {
            AttributesImpl attsImpl=new AttributesImpl(atts);
            String folderID = attsImpl.getValue("ID");
            for(Enumeration fe=fsup.getFolderAttributeNames(); fe.hasMoreElements();) {
                String attrName=(String) fe.nextElement();
                attsImpl.addAttribute("",attrName,attrName,"CDATA",fsup.getFolderAttributeValue(folderID,attrName));
                //		log.debug("adding attribute to folder="+folderID+" "+attrName+"="+fsup.getFolderAttributeValue(folderID,attrName));
            }
            super.startElement(uri,localName,qName,attsImpl);
        } else
            super.startElement(uri,localName,qName, atts);
    }
}

