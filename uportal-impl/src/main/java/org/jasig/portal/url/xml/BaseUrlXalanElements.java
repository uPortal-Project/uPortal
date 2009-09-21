/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.url.xml;

import java.util.Arrays;
import java.util.List;

import javax.xml.transform.TransformerException;

import org.apache.commons.lang.Validate;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.transformer.TransformerImpl;
import org.jasig.portal.url.IBasePortalUrl;
import org.jasig.portal.url.IPortalUrlProvider;

/**
 * Base logic for Xalan elements that deal with URL generation. Knows how to place a URL to be
 * generated in the transformer and retrieve it later. Also includes constants for transformer
 * parameters that all URL generation elements need.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public abstract class BaseUrlXalanElements<T extends IBasePortalUrl> {
    public static final String PORTAL_URL_PROVIDER_PARAMETER = IPortalUrlProvider.class.getName();
    public static final String CURRENT_PORTAL_URL = BaseUrlXalanElements.class.getName() + ".CURRENT_PORTAL_URL";
    public static final String CURRENT_PORTAL_REQUEST = BaseUrlXalanElements.class.getName() + ".CURRENT_PORTAL_REQUEST";
    
    private final Class<T> expectedUrlType;
    
    protected BaseUrlXalanElements(Class<T> expectedUrlType) {
        Validate.notNull(expectedUrlType);
        this.expectedUrlType = expectedUrlType;
    }

    protected void transform(T portalUrl, TransformerImpl transformer, ElemExtensionCall elem) throws TransformerException {
        //Note, use of wrapper list since parameters cannot be removed or set to null this lets the url
        //object be removed after the child objects complete
        transformer.setParameter(CURRENT_PORTAL_URL, Arrays.asList((Object)portalUrl));
        try {
            transformer.executeChildTemplates(elem, true);
        }
        finally {
            transformer.setParameter(CURRENT_PORTAL_URL, Arrays.asList((Object)null));
        }
    }
    
    protected T getCurrentPortalUrl(TransformerImpl transformer) {
        final List<?> urlHolder = (List<?>)transformer.getParameter(CURRENT_PORTAL_URL);
        if (urlHolder == null || urlHolder.size() == 0) {
            throw new IllegalStateException("There is no '" + this.expectedUrlType.getName() + "' available in the transformer");
        }
        return this.expectedUrlType.cast(urlHolder.get(0));
    }
}
