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

import org.jasig.portal.cache.CacheKey;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public interface TransformerConfigurationSource {
    public Map<String, Object> getTransformerParameters(HttpServletRequest request, HttpServletResponse response);
    
    /**
     * transformer.setOutputProperty(OutputKeys.INDENT, "yes");
     */
    public Properties getTransformerOutputProperties(HttpServletRequest request, HttpServletResponse response);
    
    public CacheKey getTransformerConfigurationKey(HttpServletRequest request, HttpServletResponse response);
}
