/**
 * Copyright (c) 2000-2010, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */

package org.jasig.portal.rendering.xslt;

import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.cache.CacheKey;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StaticTransformerConfigurationSource implements TransformerConfigurationSource {
    private Properties transformerOutputProperties;
    private Map<String, Object> transformerParameters;
    
    public void setTransformerOutputProperties(Properties transformerOutputProperties) {
        this.transformerOutputProperties = transformerOutputProperties;
    }

    public void setTransformerParameters(Map<String, Object> transformerParameters) {
        this.transformerParameters = transformerParameters;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getTransformerConfigurationKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getTransformerConfigurationKey(HttpServletRequest request, HttpServletResponse response) {
        return new CacheKey(
                this.transformerOutputProperties != null ? this.transformerOutputProperties.hashCode(): null,
                this.transformerParameters != null ? this.transformerParameters.hashCode(): null);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getTransformerOutputProperties(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Properties getTransformerOutputProperties(HttpServletRequest request, HttpServletResponse response) {
        return this.transformerOutputProperties;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getTransformerParameters(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Map<String, Object> getTransformerParameters(HttpServletRequest request, HttpServletResponse response) {
        return this.transformerParameters;
    }

}
