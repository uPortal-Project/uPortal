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

import org.jasig.portal.utils.SAX2FilterImpl;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

/**
 * Filters out startDocument and endDocument from the
 * channel content.
 * This filter is invoked by the ChannelManager
 * prior to passing channel content to the ChannelIncorporationFilter.
 * @author Peter Kharchenko
 * @version $Revision$
 * @deprecated IChannel rendering code will be replaced with portlet specific rendering code in a future release
 */
@Deprecated
public class ChannelSAXStreamFilter extends SAX2FilterImpl
{
    // downward
  public ChannelSAXStreamFilter (ContentHandler handler) {
    super(handler);
  }

    // upward
  public ChannelSAXStreamFilter (XMLReader parent) {
    super(parent);
  }

  public void startDocument() throws SAXException {
  }

  public void endDocument() throws SAXException {
  }

  public void processingInstruction(String target, String data) throws SAXException {
  }
}
