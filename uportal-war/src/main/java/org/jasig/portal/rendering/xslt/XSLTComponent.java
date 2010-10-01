/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.rendering.xslt;

import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.ErrorListener;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stax.StAXSource;

import org.apache.commons.logging.LogFactory;
import org.jasig.portal.rendering.PipelineEventReader;
import org.jasig.portal.rendering.PipelineEventReaderImpl;
import org.jasig.portal.rendering.StAXPipelineComponent;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.xml.ResourceLoaderURIResolver;
import org.jasig.portal.xml.stream.LocationOverridingEventAllocator;
import org.jasig.portal.xml.stream.UnknownLocation;
import org.jasig.portal.xml.stream.XMLStreamReaderAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.xml.SimpleTransformErrorListener;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XSLTComponent implements StAXPipelineComponent, BeanNameAware, ResourceLoaderAware {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final ErrorListener errorListener;
    private ResourceLoaderURIResolver uriResolver;
    private StAXPipelineComponent parentComponent;
    private TransformerSource transformerSource;
    private TransformerConfigurationSource xsltParameterSource;
    
    private String beanName;
    
    public XSLTComponent() {
        this.errorListener = new SimpleTransformErrorListener(LogFactory.getLog(this.getClass()));
    }
    
    public void setParentComponent(StAXPipelineComponent targetComponent) {
        this.parentComponent = targetComponent;
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
     * @see org.jasig.portal.rendering.StAXPipelineComponent#getXmlStreamReader(java.lang.Object, java.lang.Object)
     */
    @Override
    public PipelineEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final PipelineEventReader<XMLEventReader, XMLEvent> pipelineEventReader = this.parentComponent.getEventReader(request, response);
        
        final Transformer transformer = this.transformerSource.getTransformer(request, response);
        
        transformer.setURIResolver(this.uriResolver);
        
        if (this.xsltParameterSource != null) {
            final Map<String, Object> transformerParameters = this.xsltParameterSource.getParameters(request, response);
            if (transformerParameters != null) {
                this.logger.debug("{} - Setting Transformer Parameters: ", this.beanName, transformerParameters);
                for (final Map.Entry<String, Object> transformerParametersEntry : transformerParameters.entrySet()) {
                    final String name = transformerParametersEntry.getKey();
                    final Object value = transformerParametersEntry.getValue();
                    transformer.setParameter(name, value);
                }
            }
            
            final Properties outputProperties = this.xsltParameterSource.getOutputProperties(request, response);
            if (outputProperties != null) {
                this.logger.debug("{} - Setting Transformer Output Properties: ", this.beanName, outputProperties);
                transformer.setOutputProperties(outputProperties);
            }
        }
            
        final XMLEventReader eventReader = pipelineEventReader.getEventReader();
        
        //Wrap the event reader in a stream reader to avoid a JDK bug
        final XMLStreamReader streamReader;
        try {
            streamReader = new XMLStreamReaderAdapter(eventReader);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        final Source xmlReaderSource = new StAXSource(streamReader);
        
        //Setup logging for the transform
        transformer.setErrorListener(this.errorListener);

        //Transform to a DOM to avoid JDK bug: http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6775588
        final DOMResult outputTarget = new DOMResult();
        try {
            this.logger.debug("{} - Begining XML Transformation", this.beanName);
            transformer.transform(xmlReaderSource, outputTarget);
            this.logger.debug("{} - XML Transformation complete", this.beanName);
        }
        catch (TransformerException e) {
            throw new RuntimeException("Failed to transform document", e);
        }
        
        //TODO XMLInputFactory can be shared once created and configured. Need a central place for doing that
        final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
        inputFactory.setEventAllocator(new LocationOverridingEventAllocator(new UnknownLocation()));
        
        final DOMSource layoutSoure = new DOMSource(outputTarget.getNode());
        final XMLEventReader eventWriterBuffer;
        try {
            eventWriterBuffer = inputFactory.createXMLEventReader(layoutSoure);
        }
        catch (XMLStreamException e) {
            throw new RuntimeException(e);
        }
        
        return new PipelineEventReaderImpl<XMLEventReader, XMLEvent>(eventWriterBuffer);
    }

    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        final CacheKey parentCacheKey = this.parentComponent.getCacheKey(request, response);
        
        final CacheKey transformerKey;
        if (transformerSource != null) {
            transformerKey = this.transformerSource.getCacheKey(request, response);
        }
        else {
            transformerKey = null;
        }
        
        final CacheKey transformerConfigurationKey;
        if (this.xsltParameterSource != null) {
            transformerConfigurationKey = this.xsltParameterSource.getCacheKey(request, response);
        }
        else {
            transformerConfigurationKey = null;
        }
        
        return new CacheKey(this.beanName, parentCacheKey, transformerKey, transformerConfigurationKey);
    }
}
