/* Copyright 2005 The JA-SIG Collaborative.  All rights reserved.
*  See license distributed with this file and
*  available online at http://www.uportal.org/license.html
*/
package org.jasig.portal.channels.adminnav;

import java.util.HashMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Handler for receiving SAX events from parsing a set of links having the
 * following structure.
 * 
 * <pre>
 *  &lt;links [bundleResolver="bundleBase"]>
 *   &lt;link labelId="id-unique-within-resolver"
 *            channelFunctionalName="functional-name-of-channel">
 *    [&lt;parameter name="some-Name" value="some-value" />
 *     &lt;parameter ..../>
 *    ]
 *   &lt;link...>...&lt;/link>
 *  &lt;/links>
 * </pre>
 * 
 * If an instance of ILabelResolver is not passed into this class and there
 * is no bundleResolver declared by the links element then an 
 * IllegalStateException will be thrown. Similarly, if a null 
 * ILinkRegistrar instance is passed in an IllegalStateException will be 
 * thrown.
 * 
 * @author mboyd@sungardsct.com
 * @since 2.6
 */
public class XMLLinksHandler extends DefaultHandler
{
    private String labelId = null;
    private String fname = null;
    private HashMap parameters = new HashMap();
    private ILabelResolver globalResolver = null;
    private ILabelResolver localResolver = null;
    private ILinkRegistrar registrar = null;

    /**
     * Default constructor which obtains an instance of ILinkRegistrar from 
     * AdminNavChannel's static getLinkRegistrar() method.
     *
     */
    public XMLLinksHandler()
    {
        this(AdminNavChannel.getLinkRegistrar());
    }

    /**
     * Constructor accepting an instance of ILinkRegistrar into which all link 
     * definitions will be added.
     *
     */
    public XMLLinksHandler(ILinkRegistrar registrar)
    {
        this(registrar, null);
    }

    /**
     * Constructor accepting an instance of ILinkRegistrar into which all link 
     * definitions will be added. Also accepts an ILabelResolver instance 
     * which will be used to resolve labels for all links unless a 
     * bundleResolver is specified on the outermost links element.
     *
     */
    public XMLLinksHandler(ILinkRegistrar registrar, ILabelResolver resolver)
    {
        if (registrar == null)
            throw new IllegalStateException("Link Registrar can " +
                    "not be null.");
        this.registrar = registrar;
        this.globalResolver = resolver;
    }

    public void startElement(
        String nsURI,
        String localName,
        String qName,
        Attributes atts)
        throws SAXException
    {
        if (qName.equals("links"))
        {
            String bundleBase = atts.getValue("bundleResolver");
            if (bundleBase != null && ! bundleBase.equals(""))
                localResolver = new ResourceBundleResolver(bundleBase);
        }
        else if (qName.equals("link"))
        {
            fname = atts.getValue("channelFunctionalName");
            labelId = atts.getValue("labelId");
        }
        else if (qName.equals("parameter"))
        {
            String name = atts.getValue("name");
            String value = atts.getValue("value");
            parameters.put(name, value);
        }
    }
    
    public void endElement(String uri, String localName, String qName)
            throws SAXException
    {
        if (qName.equals("link"))
        {
            if (localResolver == null && globalResolver == null)
                throw new IllegalStateException("No resolver available " +
                        "for link label resolution.");
            if (localResolver != null)
                registrar.addLink(fname, labelId, localResolver, parameters);
            else
            registrar.addLink(fname, labelId, globalResolver, parameters);
            parameters.clear();
        }
        if (qName.equals("links"))
        {
            localResolver = null;
        }
    }
}
