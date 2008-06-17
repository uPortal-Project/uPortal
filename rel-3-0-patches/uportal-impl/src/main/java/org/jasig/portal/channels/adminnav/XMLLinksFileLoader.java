/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
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
 */
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
