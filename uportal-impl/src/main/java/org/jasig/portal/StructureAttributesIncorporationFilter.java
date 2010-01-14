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

