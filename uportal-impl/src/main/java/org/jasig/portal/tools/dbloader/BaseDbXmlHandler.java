/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.tools.dbloader;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jasig.portal.utils.DTDResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Base SAX handler for providing an entity resolver and capturing character data
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class BaseDbXmlHandler extends DefaultHandler2 {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private final DTDResolver resolver = new DTDResolver();

    protected StringBuilder chars;
    
    
    
    /* (non-Javadoc)
     * @see org.xml.sax.ext.DefaultHandler2#resolveEntity(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId) throws SAXException, IOException {
        return resolver.resolveEntity(publicId, systemId);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.DefaultHandler2#resolveEntity(java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        return resolver.resolveEntity(publicId, systemId);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.helpers.DefaultHandler#characters(char[], int, int)
     */
    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (this.chars != null) {
            this.chars.append(ch, start, length);
        }
    }
}
