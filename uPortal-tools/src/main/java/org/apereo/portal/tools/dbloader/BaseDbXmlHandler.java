/**
 * Licensed to Apereo under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. Apereo
 * licenses this file to you under the Apache License, Version 2.0 (the "License"); you may not use
 * this file except in compliance with the License. You may obtain a copy of the License at the
 * following location:
 *
 * <p>http://www.apache.org/licenses/LICENSE-2.0
 *
 * <p>Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apereo.portal.tools.dbloader;

import java.io.IOException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.utils.DTDResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

/**
 * Base SAX handler for providing an entity resolver and capturing character data
 *
 */
public class BaseDbXmlHandler extends DefaultHandler2 {
    protected final Log logger = LogFactory.getLog(this.getClass());

    private final DTDResolver resolver = new DTDResolver();

    protected StringBuilder chars;

    /* (non-Javadoc)
     * @see org.xml.sax.ext.DefaultHandler2#resolveEntity(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String name, String publicId, String baseURI, String systemId)
            throws SAXException, IOException {
        return resolver.resolveEntity(publicId, systemId);
    }

    /* (non-Javadoc)
     * @see org.xml.sax.ext.DefaultHandler2#resolveEntity(java.lang.String, java.lang.String)
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId)
            throws SAXException, IOException {
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
