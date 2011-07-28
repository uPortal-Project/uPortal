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

package org.jasig.portal.io.xml.crn;

import java.io.IOException;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;

import org.dom4j.Element;
import org.dom4j.io.DocumentSource;
import org.jasig.portal.io.xml.IDataExporter;
import org.jasig.portal.io.xml.IPortalData;
import org.jasig.portal.io.xml.IPortalDataType;
import org.jasig.portal.utils.SafeFilenameUtils;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.xml.XmlUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.oxm.Marshaller;
import org.springframework.oxm.XmlMappingException;

import com.google.common.base.Function;

/**
 * Base class used for exporting DOM4j data
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class AbstractDom4jExporter implements IDataExporter<Tuple<String, Element>>, Marshaller  {
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private XmlUtilities xmlUtilities;
    private IPortalDataType portalDataType;
    private Function<IPortalDataType, Iterable<? extends IPortalData>> portalDataRetriever;

    @Autowired
    public void setXmlUtilities(XmlUtilities xmlUtilities) {
        this.xmlUtilities = xmlUtilities;
    }

    @Required
    public void setPortalDataType(IPortalDataType portalDataType) {
        this.portalDataType = portalDataType;
    }

    @Required
    public void setPortalDataRetriever(Function<IPortalDataType, Iterable<? extends IPortalData>> portalDataRetriever) {
        this.portalDataRetriever = portalDataRetriever;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataExporter#getPortalDataType()
     */
    @Override
    public IPortalDataType getPortalDataType() {
        return this.portalDataType;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataExporter#getPortalData()
     */
    @Override
    public Iterable<? extends IPortalData> getPortalData() {
        return this.portalDataRetriever.apply(this.portalDataType);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataExporter#exportData(java.lang.String)
     */
    @Override
    public Tuple<String, Element> exportData(String id) {
        final Element node = this.exportDataElement(id);
        if (node == null) {
            return null;
        }
        
        return new Tuple<String, Element>(id, node);
    }
    
    protected abstract Element exportDataElement(String id);

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataExporter#getFileName(java.lang.Object)
     */
    @Override
    public String getFileName(Tuple<String, Element> data) {
        return SafeFilenameUtils.makeSafeFilename(data.first);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.io.xml.IDataExporter#getMarshaller()
     */
    @Override
    public Marshaller getMarshaller() {
        return this;
    }

    /* (non-Javadoc)
     * @see org.springframework.oxm.Marshaller#supports(java.lang.Class)
     */
    @Override
    public boolean supports(Class<?> clazz) {
        return true;
    }

    /* (non-Javadoc)
     * @see org.springframework.oxm.Marshaller#marshal(java.lang.Object, javax.xml.transform.Result)
     */
    @Override
    public void marshal(Object graph, Result result) throws IOException, XmlMappingException {
        @SuppressWarnings("unchecked")
        final Tuple<String, Element> data = (Tuple<String, Element>)graph;
        
        final Transformer transformer;
        try {
            transformer = this.xmlUtilities.getIdentityTransformer();
        }
        catch (TransformerConfigurationException e) {
            throw new RuntimeException("Failed to load identity Transformer", e);
        }

        //Setup the transformer to pretty-print the output
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        
        try {
            transformer.transform(new DocumentSource(data.second), result);
        }
        catch (TransformerException e) {
            throw new RuntimeException("Failed to write Element to Result for: " + data.first, e);
        }
    }
}
