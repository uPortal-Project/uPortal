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
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;

import org.jasig.portal.rendering.CacheableEventReader;
import org.jasig.portal.rendering.CacheableEventReaderImpl;
import org.jasig.portal.rendering.StAXPipelineComponent;
import org.jasig.portal.utils.cache.CacheKey;
import org.jasig.portal.xml.stream.XMLEventWriterBuffer;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class XSLTComponent implements StAXPipelineComponent {
    private StAXPipelineComponent parentComponent;
    private TransformerSource transformerSource;
    private TransformerConfigurationSource xsltParameterSource;
    
    
    public void setParentComponent(StAXPipelineComponent targetComponent) {
        this.parentComponent = targetComponent;
    }
    public void setXsltParameterSource(TransformerConfigurationSource xsltParameterSource) {
        this.xsltParameterSource = xsltParameterSource;
    }
    public void setTransformerSource(TransformerSource transformerSource) {
        this.transformerSource = transformerSource;
    }
    
    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.StAXPipelineComponent#getXmlStreamReader(java.lang.Object, java.lang.Object)
     */
    @Override
    public CacheableEventReader<XMLEventReader, XMLEvent> getEventReader(HttpServletRequest request, HttpServletResponse response) {
        final CacheableEventReader<XMLEventReader, XMLEvent> eventReader = this.parentComponent.getEventReader(request, response);
        
        final XMLEventWriterBuffer eventWriterBuffer = new XMLEventWriterBuffer();
        
        final Transformer transformer = this.transformerSource.getTransformer(request, response);
        final CacheKey transformerKey = this.transformerSource.getCacheKey(request, response);

        //Setup transformer parameters
        final CacheKey transformerConfigurationKey = this.setupTransformer(request, response, transformer);
            
        final StAXSource xmlSource;
        try {
            xmlSource = new StAXSource(eventReader.getEventReader());
        }
        catch (XMLStreamException e) {
            throw new RuntimeException("Failed to create StAXSource from XMLEventReader", e);
        }
        
        final StAXResult outputTarget = new StAXResult(eventWriterBuffer);
        try {
            transformer.transform(xmlSource, outputTarget);
        }
        catch (TransformerException e) {
            throw new RuntimeException("Failed to transform document", e);
        }
        
        return new CacheableEventReaderImpl<XMLEventReader, XMLEvent>(
                this.buildCacheKey(eventReader.getCacheKey(), transformerKey, transformerConfigurationKey),
                eventWriterBuffer);
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
        
        return buildCacheKey(parentCacheKey, transformerKey, transformerConfigurationKey);
    }
    
    /**
     * Build the cache key used for XSLT
     */
    protected CacheKey buildCacheKey(CacheKey parentCacheKey, CacheKey transformerKey, CacheKey transformerConfigurationKey) {
        return new CacheKey(parentCacheKey, transformerKey, transformerConfigurationKey);
    }
    
    /**
     * @return a hash code to be used for cache key generation
     */
    protected CacheKey setupTransformer(HttpServletRequest request, HttpServletResponse response, final Transformer transformer) {
        if (this.xsltParameterSource == null) {
            return null;
        }
        
        final Map<String, Object> transformerParameters = this.xsltParameterSource.getParameters(request, response);
        if (transformerParameters != null) {
            for (final Map.Entry<String, Object> transformerParametersEntry : transformerParameters.entrySet()) {
                final String name = transformerParametersEntry.getKey();
                final Object value = transformerParametersEntry.getValue();
                transformer.setParameter(name, value);
            }
        }
        
        final Properties outputProperties = this.xsltParameterSource.getOutputProperties(request, response);
        if (outputProperties != null) {
            transformer.setOutputProperties(outputProperties);
        }
        
        return this.xsltParameterSource.getCacheKey(request, response);
    }
}
