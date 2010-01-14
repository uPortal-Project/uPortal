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

package org.jasig.portal.channels.adminnav;

import java.io.InputStream;
import org.jasig.portal.utils.DTDResolver;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.xml.sax.ContentHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

/**
 * Loads links defined in the passed in file path having the XML format
 * specified in javadocs for XMLLinksHandler.
 *
 * @author mboyd@sungardsct.com
 * @since 2.6
 * @deprecated All IChannel implementations should be migrated to portlets
 */
@Deprecated
public class XMLLinksFileLoader
{
    private static Log LOG = LogFactory.getLog(XMLLinksFileLoader.class);

    /**
     * Loads into the passed in ILinkRegistrar the links specified in the passed
     * in file which must conform to the structure of the XMLLinksHandler.
     */
    public XMLLinksFileLoader(String linksFile, ILinkRegistrar registrar,
            ILabelResolver resolver)
    {
        XMLReader parser = null;
        try
        {
            parser = XMLReaderFactory.createXMLReader();
            parser.setEntityResolver(new DTDResolver("adminNav.dtd"));
        }
        catch (SAXException e)
        {
            throw new RuntimeException( "Unable to create an XML reader.", e);
        }

        InputStream stream = this.getClass().getResourceAsStream(linksFile);

        if (stream == null)
        {
            throw new RuntimeException("No links configuration "
                    + "file " + linksFile + " found. No statically "
                    + "defined links are available for the list.");
        }

        InputSource source = new InputSource(stream);
        ContentHandler ch = (ContentHandler) new XMLLinksHandler(registrar,
                resolver);
        parser.setContentHandler(ch);

        try
        {
            parser.parse(source);
        }
        catch (Exception se)
        {
            throw new RuntimeException(
                "Unable to load links from " + linksFile + ".", se);
        }
    }
}
