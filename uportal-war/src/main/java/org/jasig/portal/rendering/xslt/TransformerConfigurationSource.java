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
import javax.xml.transform.Transformer;

import org.jasig.portal.utils.cache.CacheKey;

/**
 * Used by a {@link XSLTComponent} to configure the {@link Transformer}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface TransformerConfigurationSource {
    /**
     * Passed on to {@link Transformer#setParameter(String, Object)}
     */
    public Map<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * Passed on to {@link Transformer#setOutputProperties(Properties)}
     */
    public Properties getOutputProperties(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * A key representing the state of the parameters and properties for the request.
     */
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response);
}
