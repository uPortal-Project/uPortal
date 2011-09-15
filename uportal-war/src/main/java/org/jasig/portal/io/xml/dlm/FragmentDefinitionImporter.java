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

package org.jasig.portal.io.xml.dlm;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stax.StAXSource;
import javax.xml.xpath.XPathConstants;

import org.jasig.portal.io.xml.IDataImporter;
import org.jasig.portal.io.xml.PortalDataKey;
import org.jasig.portal.layout.dlm.FragmentDefinition;
import org.jasig.portal.layout.dlm.IFragmentDefinitionDao;
import org.jasig.portal.utils.Tuple;
import org.jasig.portal.xml.XmlUtilities;
import org.jasig.portal.xml.xpath.XPathOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.oxm.Unmarshaller;
import org.springframework.oxm.XmlMappingException;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.xml.FixedXMLEventStreamReader;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Imports a uPortal 3.1 style fragment definition data file
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class FragmentDefinitionImporter implements IDataImporter<Tuple<String, Document>>, Unmarshaller {
	
	private IFragmentDefinitionDao fragmentDefinitionDao;
	private XmlUtilities xmlUtilities;
	private XPathOperations xPathOperations;
    
	@Autowired
	public void setFragmentDefinitionDao(IFragmentDefinitionDao fragmentDefinitionDao) {
		this.fragmentDefinitionDao = fragmentDefinitionDao;
	}

	@Autowired
	public void setXmlUtilities(XmlUtilities xmlUtilities) {
		this.xmlUtilities = xmlUtilities;
	}

	@Autowired
	public void setxPathOperations(XPathOperations xPathOperations) {
		this.xPathOperations = xPathOperations;
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataImporter#getImportDataKeys()
	 */
	@Override
	public Set<PortalDataKey> getImportDataKeys() {
		return Collections.singleton(FragmentDefinitionPortalDataType.IMPORT_31_DATA_KEY);
	}

	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataImporter#importData(java.lang.Object)
	 */
	@Override
	@Transactional
	public void importData(Tuple<String, Document> data) {
		final Document resultDoc = data.second;
		
		final Element fragmentDefElement = this.xPathOperations.evaluate("//*[local-name() = 'fragment']", resultDoc, XPathConstants.NODE);
		if (fragmentDefElement == null) {
			throw new IllegalArgumentException("Could not find required dlm:fragment element in fragment-definition file");
		}
		final String fragmentName = fragmentDefElement.getAttribute("name");
		
		FragmentDefinition fragmentDefinition = this.fragmentDefinitionDao.getFragmentDefinition(fragmentName);
		if (fragmentDefinition == null) {
			fragmentDefinition = new FragmentDefinition(fragmentDefElement);
		}
		
		fragmentDefinition.loadFromEelement(fragmentDefElement);
		
		this.fragmentDefinitionDao.updateFragmentDefinition(fragmentDefinition);
	}
	/* (non-Javadoc)
	 * @see org.jasig.portal.io.xml.IDataImporter#getUnmarshaller()
	 */
	@Override
	public Unmarshaller getUnmarshaller() {
		return this;
	}

	/* (non-Javadoc)
	 * @see org.springframework.oxm.Unmarshaller#supports(java.lang.Class)
	 */
	@Override
	public boolean supports(Class<?> clazz) {
        throw new UnsupportedOperationException();
	}

	/* (non-Javadoc)
	 * @see org.springframework.oxm.Unmarshaller#unmarshal(javax.xml.transform.Source)
	 */
	@Override
	public Object unmarshal(Source source) throws IOException, XmlMappingException {
		if (source instanceof StAXSource) {
			source = fixStAXSource((StAXSource)source);
		}
		
		final Transformer identityTransformer;
		try {
			identityTransformer = this.xmlUtilities.getIdentityTransformer();
		}
		catch (TransformerConfigurationException e) {
			throw new RuntimeException("Failed to create identity Transformer", e);
		}
		
		final DOMResult domResult = new DOMResult();
		try {
			identityTransformer.transform(source, domResult);
		}
		catch (TransformerException e) {
			throw new RuntimeException("Failed to transform " + source + " into Document", e);
		}
		
        final Document resultDoc = (Document)domResult.getNode();
		return new Tuple<String, Document>(source.getSystemId(), resultDoc);
	}
	
	protected Source fixStAXSource(StAXSource staxSource) {
        XMLStreamReader xmlStreamReader = staxSource.getXMLStreamReader();
        if (xmlStreamReader != null) {
        	return staxSource;
        }
        
        final XMLEventReader xmlEventReader = staxSource.getXMLEventReader();
        
        //Wrap the event reader in a stream reader to avoid a JDK bug
        final XMLStreamReader streamReader;
        try {
            streamReader = new VersionCachingXMLEventStreamReader(xmlEventReader);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create XMLStreamReader from XMLEventReader", e);
        }
        
        return new StAXSource(streamReader);
	}
	
	private static class VersionCachingXMLEventStreamReader extends FixedXMLEventStreamReader {
		private String version;

		public VersionCachingXMLEventStreamReader(XMLEventReader eventReader) throws XMLStreamException {
			super(eventReader);
			
			if (this.getEventType() == XMLEvent.START_DOCUMENT) {
				version = super.getVersion();
			}
		}

		/* (non-Javadoc)
		 * @see org.springframework.util.xml.FixedXMLEventStreamReader#getVersion()
		 */
		@Override
		public String getVersion() {
			final String superVersion = super.getVersion();
			if (superVersion != null) {
				return superVersion;
			}
			
			return version;
		}

		/* (non-Javadoc)
		 * @see org.springframework.util.xml.FixedXMLEventStreamReader#next()
		 */
		@Override
		public int next() throws XMLStreamException {
			final int eventType = super.next();
			if (eventType == XMLEvent.START_DOCUMENT) {
				version = super.getVersion();
			}
			
			return eventType;
		}
		
		
	}
}
