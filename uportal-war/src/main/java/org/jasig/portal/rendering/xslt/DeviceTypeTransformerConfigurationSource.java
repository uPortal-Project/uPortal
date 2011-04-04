package org.jasig.portal.rendering.xslt;

import java.util.Collections;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jasig.portal.utils.cache.CacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DeviceTypeTransformerConfigurationSource extends TransformerConfigurationSourceAdapter {
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getParameters(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public Map<String, Object> getParameters(HttpServletRequest request, HttpServletResponse response) {
        
        final boolean isNative = Boolean.valueOf((String) request.getSession(false).getAttribute("isNativeDevice"));
        return Collections.singletonMap("NATIVE", (Object) isNative);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.rendering.xslt.TransformerConfigurationSource#getCacheKey(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
     */
    @Override
    public CacheKey getCacheKey(HttpServletRequest request, HttpServletResponse response) {
        
        return new CacheKey("LocaleTransformerConfigurationSource", request.getHeader("user-agent"));
    }

}
