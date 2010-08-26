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

package org.jasig.portal.xml;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.stream.XMLEventFactory;
import javax.xml.transform.Templates;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamSource;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.springframework.core.io.Resource;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XmlUtilitiesImpl implements XmlUtilities {
    private final XMLEventFactory xmlEventFactory = XMLEventFactory.newFactory();
    
    private Ehcache templatesCache;

    public void setTemplatesCache(Ehcache templatesCache) {
        this.templatesCache = templatesCache;
    }

    public Templates getTemplates(Resource stylesheet) throws TransformerConfigurationException, IOException {
        final String key = stylesheet.getDescription();
        Element templatesElement = this.templatesCache.get(key);
        if (templatesElement != null) {
            return (Templates)templatesElement.getObjectValue();
        }
        
        final InputStream stylesheetStream = stylesheet.getInputStream();

        final TransformerFactory transformerFactory = TransformerFactory.newInstance();
        final Templates templates = transformerFactory.newTemplates(new StreamSource(stylesheetStream));
        
        templatesElement = new Element(key, templates);
        this.templatesCache.put(templatesElement);
        
        return templates;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.xml.XmlUtilities#getTransformer(org.springframework.core.io.Resource)
     */
    @Override
    public Transformer getTransformer(Resource stylesheet) throws TransformerConfigurationException, IOException {
        final Templates templates = this.getTemplates(stylesheet);
        return templates.newTransformer();
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.xml.XmlUtilities#getXmlEventFactory()
     */
    @Override
    public XMLEventFactory getXmlEventFactory() {
        return this.xmlEventFactory;
    }

}
