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

package org.jasig.portal.channels;

import org.jasig.portal.ChannelStaticData;
import org.jasig.portal.ChannelRuntimeData;
import org.jasig.portal.PortalException;
import org.jasig.portal.security.IPerson;
import org.jasig.portal.services.LogService;
import org.jasig.portal.services.PersonDirectory;
import org.jasig.portal.utils.XML;
import org.jasig.portal.utils.XSLT;
import org.jasig.portal.utils.ResourceLoader;
import org.jasig.portal.utils.DocumentFactory;
import org.xml.sax.ContentHandler;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import javax.xml.transform.TransformerException;
import  java.util.Iterator;
/**
 * This channel demonstrates the method of obtaining and displaying
 * standard uPortal person attributes.
 * @author Ken Weiner, kweiner@interactivebusiness.com
 * @version $Revision$
 */
public class CPersonAttributes extends BaseMultithreadedChannel {
  private static final String sslLocation = "CPersonAttributes/CPersonAttributes.ssl";

  public void renderXML (ContentHandler out, String uid) throws PortalException {
    ChannelState channelState = (ChannelState)channelStateMap.get(uid);
    ChannelStaticData staticData = channelState.getStaticData();
    ChannelRuntimeData runtimeData = channelState.getRuntimeData();
    IPerson person = staticData.getPerson();
    Document doc = DocumentFactory.getNewDocument();

    Element attributesE = doc.createElement("attributes");

    // Grab all the name elements from eduPerson.xml
    Iterator attribs = PersonDirectory.getPropertyNamesIterator();
    while ( attribs.hasNext() ) {
      // Get the attribute name
      String attName = (String)attribs.next();
      // Set the attribute
      Element attributeE = doc.createElement("attribute");

      Element nameE = doc.createElement("name");
      nameE.appendChild(doc.createTextNode(attName));
      attributeE.appendChild(nameE);

      // Get the IPerson attribute value for this eduPerson attribute name
      String value = (String)person.getAttribute(attName);
      if (value != null) {
        Element valueE = doc.createElement("value");
        valueE.appendChild(doc.createTextNode(value));
        attributeE.appendChild(valueE);
      }

      attributesE.appendChild(attributeE);
    }

    doc.appendChild(attributesE);

    XSLT xslt = new XSLT(this);
    xslt.setXML(doc);
    xslt.setXSL(sslLocation, runtimeData.getBrowserInfo());
    xslt.setTarget(out);
    xslt.transform();
  }

}