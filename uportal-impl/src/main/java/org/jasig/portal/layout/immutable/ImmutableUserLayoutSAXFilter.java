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

package org.jasig.portal.layout.immutable;

import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.AttributesImpl;

/**
 * This filter will mark all folders and channels as unremovable and immutable.
 * 
 * Prior to uPortal 2.5, this class existed in the package org.jasig.portal.layout.
 * It was moved to its present package to associate it with the 
 * ImmutableUserLayoutManagerWrapper and to separate it from the general Layout API.
 * 
 * @author Peter Kharchenko  {@link <a href="mailto:pkharchenko@interactivebusiness.com"">pkharchenko@interactivebusiness.com"</a>}
 * @version 1.0 $Revision$ $Date$
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

