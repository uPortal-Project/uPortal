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
package org.apereo.portal.rendering.xslt;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXResult;
import javax.xml.transform.stax.StAXSource;
import org.apache.commons.logging.LogFactory;
import org.apereo.portal.rendering.PipelineEventReader;
import org.apereo.portal.rendering.PipelineEventReaderImpl;
import org.apereo.portal.rendering.StAXPipelineComponentWrapper;
import org.apereo.portal.utils.cache.CacheKey;
import org.apereo.portal.xml.ResourceLoaderURIResolver;
import org.apereo.portal.xml.StaxUtils;
import org.apereo.portal.xml.stream.XMLEventBufferReader;
import org.apereo.portal.xml.stream.XMLEventBufferWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.xml.FixedXMLEventStreamReader;
import org.springframework.util.xml.SimpleTransformErrorListener;
import org.xml.sax.ContentHandler;
import org.xml.sax.helpers.LocatorImpl;

/**
 */
public class XSLTComponent extends StAXPipelineComponentWrapper
        implements BeanNameAware, ResourceLoaderAware {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ErrorListener errorListener;
    private ResourceLoaderURIResolver uriResolver;
    private TransformerSource transformerSource;
    private TransformerConfigurationSource xsltParameterSource;

    private String beanName;

    public XSLTComponent() {
        this.errorListener = new SimpleTransformErrorListener(LogFactory.getLog(this.getClass()));
    }

    public void setXsltParameterSource(TransformerConfigurationSource xsltParameterSource) {
        this.xsltParameterSource = xsltParameterSource;
    }

    public void setTransformerSource(TransformerSource transformerSource) {
        this.transformerSource = transformerSource;
    }

    @Override
    public void setBeanName(String name) {
        this.beanName = name;
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.uriResolver = new ResourceLoaderURIResolver(resourceLoader);
    }

    /* (non-Javadoc)
     * @see org.apereo.portal.rendering.StAXPipelineComponent#getXmlStreamReader(java.lang.Object, java.lang.Object)
     */
    @Override
    public PipelineEventReader<XMLEventReader, XMLEvent> getEventReader(
            HttpServletRequest request, HttpServletResponse response) {
        final PipelineEventReader<XMLEventReader, XMLEvent> pipelineEventReader =
                this.wrappedComponent.getEventReader(request, response);

        final Transformer transformer = this.transformerSource.getTransformer(request, response);

        //Setup a URIResolver based on the current resource loader
        transformer.setURIResolver(this.uriResolver);

        //Configure the Transformer via injected class
        if (this.xsltParameterSource != null) {
            final Map<String, Object> transformerParameters =
                    this.xsltParameterSource.getParameters(request, response);
            if (transformerParameters != null) {
                this.logger.debug(
                        "{} - Setting Transformer Parameters: ",
                        this.beanName,
                        transformerParameters);
                for (final Map.Entry<String, Object> transformerParametersEntry :
                        transformerParameters.entrySet()) {
                    final String name = transformerParametersEntry.getKey();
                    final Object value = transformerParametersEntry.getValue();
                    if (value != null) {
                        transformer.setParameter(name, value);
                    }
                }
            }

            final Properties outputProperties =
                    this.xsltParameterSource.getOutputProperties(request, response);
            if (outputProperties != null) {
                this.logger.debug(
                        "{} - Setting Transformer Output Properties: ",
                        this.beanName,
                        outputProperties);
                transformer.setOutputProperties(outputProperties);
            }
        }

        //The event reader from the previous component in the pipeline
        final XMLEventReader eventReader = pipelineEventReader.getEventReader();

        //Wrap the event reader in a stream reader to avoid a JDK bug
        final XMLStreamReader streamReader;
        try {
            streamReader = new FixedXMLEventStreamReader(eventReader);
        } catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create XMLStreamReader from XMLEventReader", e);
        }
        final Source xmlReaderSource = new StAXSource(streamReader);

        //Setup logging for the transform
        transformer.setErrorListener(this.errorListener);

        //Transform to a SAX ContentHandler to avoid JDK bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6775588
        final XMLEventBufferWriter eventWriterBuffer = new XMLEventBufferWriter();
        final ContentHandler contentHandler =
                StaxUtils.createLexicalContentHandler(eventWriterBuffer);
        contentHandler.setDocumentLocator(new LocatorImpl());

        final SAXResult outputTarget = new SAXResult(contentHandler);
        try {
            this.logger.debug("{} - Begining XML Transformation", this.beanName);
            transformer.transform(xmlReaderSource, outputTarget);
            this.logger.debug("{} - XML Transformation complete", this.beanName);
        } catch (TransformerException e) {
            throw new RuntimeException("Failed to transform document", e);
        }

        final String mediaType = transformer.getOutputProperty(OutputKeys.MEDIA_TYPE);

        final List<XMLEvent> eventBuffer = eventWriterBuffer.getEventBuffer();
        final XMLEventReader outputEventReader =
                new XMLEventBufferReader(eventBuffer.listIterator());

        final Map<String, String> outputProperties = pipelineEventReader.getOutputProperties();
        final PipelineEventReaderImpl<XMLEventReader, XMLEvent> pipelineEventReaderImpl =
                new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(
                        outputEventReader, outputProperties);
        pipelineEventReaderImpl.setOutputProperty(OutputKeys.MEDIA_TYPE, mediaType);
        return pipelineEventReaderImpl;
    }

    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final CacheKey parentCacheKey = this.wrappedComponent.getCacheKey(request, response);

        final CacheKey transformerKey;
        if (transformerSource != null) {
            transformerKey = this.transformerSource.getCacheKey(request, response);
        } else {
            transformerKey = null;
        }

        final CacheKey transformerConfigurationKey;
        if (this.xsltParameterSource != null) {
            transformerConfigurationKey = this.xsltParameterSource.getCacheKey(request, response);
        } else {
            transformerConfigurationKey = null;
        }

        return CacheKey.build(
                this.beanName, parentCacheKey, transformerKey, transformerConfigurationKey);
    }
}
