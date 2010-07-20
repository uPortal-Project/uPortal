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
import org.apache.xalan.extensions.XSLProcessorContext;
import org.apache.xalan.templates.ElemExtensionCall;
import org.apache.xalan.transformer.TransformerImpl;
import org.jasig.portal.portlet.registry.NotAPortletException;
import org.jasig.portal.url.IBasePortalUrl;
import org.jasig.portal.url.IPortalUrlProvider;
import org.w3c.dom.Node;

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

    /**
     * Creates, configures and returns a URL based on the {@link IBasePortalUrl} API. Calls to the abstract
     * {@link #createUrl(XSLProcessorContext, ElemExtensionCall)} API to generate the URL object, stores the
     * object as a transformer parameter, calls {@link TransformerImpl#executeChildTemplates(org.apache.xalan.templates.ElemTemplateElement, boolean)}
     * then lets sub-classes post-process the URL as needed via {@link #postProcessUrl(IBasePortalUrl, XSLProcessorContext, ElemExtensionCall)}
     * before returning the result of {@link IBasePortalUrl#getUrlString()}
     */
    public String url(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        final TransformerImpl transformer = context.getTransformer();
        
        try {
            // retrieve configuration
            final T url = this.createUrl(context, elem);
            
            this.transform(url, transformer, elem);
            
            this.postProcessUrl(url, context, elem);
            
            return url.getUrlString();
        }
        catch (Throwable t) {
            if (t instanceof NotAPortletException) {
                return "NOT_A_PORTLET";
            }
            
            if (t instanceof TransformerException) {
                final TransformerException te = (TransformerException)t;
                transformer.setExceptionThrown(te);
                throw te;
            }
            
            final RuntimeException re;
            if (t instanceof RuntimeException) {
                re = (RuntimeException)t;
            }
            else {
                re = new RuntimeException(t);
            }
            
            transformer.setExceptionThrown(re);
            throw re;
        }
    }
    
    /**
     * Handles a param element with a name attribute and either a value attribute or content within the element.
     * Determines the appropriate value source and calls {@link #addParameter(IBasePortalUrl, String, String)}
     * with the data.
     */
    public void param(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        final TransformerImpl transformer = context.getTransformer();
        
        try {
            // retrieve configuration
            final T url = this.getCurrentPortalUrl(transformer);
            
            final Node contextNode = context.getContextNode();
            final String name = elem.getAttribute("name", contextNode, transformer);
            String value = elem.getAttribute("value", contextNode, transformer);
            
            //No value attribute, try running any nested part of the XSLT
            if (value == null) {
                final StringBuildingContentHandler contentHandler = new StringBuildingContentHandler(transformer);
                transformer.executeChildTemplates(elem, contentHandler);
                value = contentHandler.toString();
            }

            this.addParameter(url, name, value);
        }
        catch (Throwable t) {
            if (t instanceof TransformerException) {
                throw (TransformerException)t;
            }
            
            final RuntimeException re;
            if (t instanceof RuntimeException) {
                re = (RuntimeException)t;
            }
            else {
                re = new RuntimeException(t);
            }
            
            transformer.setExceptionThrown(re);
            throw re;
        }
    }
    
    /**
     * Create a new {@link IBasePortalUrl} or subclass based on the element information
     */
    protected abstract T createUrl(XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException;
    
    /**
     * Optional method allowing additional manipulation of the URL object before it is returned
     * as a string. Called after {@link TransformerImpl#executeChildTemplates(org.apache.xalan.templates.ElemTemplateElement, boolean)}
     * is called on the url element.
     */
    @SuppressWarnings("unused")
    protected void postProcessUrl(T url, XSLProcessorContext context, ElemExtensionCall elem) throws TransformerException {
        //NOOP
    }
    
    /**
     * Called by {@link #param(XSLProcessorContext, ElemExtensionCall)}, must be implemented by the subclass for the param
     * method to function
     */
    protected void addParameter(T url, String name, String value) {
        throw new UnsupportedOperationException("The param function is not implemented for '" + this.expectedUrlType.getName() + "' URLs");
    }

    /**
     * Called by {@link #url(XSLProcessorContext, ElemExtensionCall)} to store the URL in the transformer, execute
     * the child templates and then remove the URL from the transformer
     */
    protected void transform(T url, TransformerImpl transformer, ElemExtensionCall elem) throws TransformerException {
        //Note, use of wrapper list since parameters cannot be removed or set to null this lets the url
        //object be removed after the child objects complete
        transformer.setParameter(CURRENT_PORTAL_URL, Arrays.asList((Object)url));
        try {
            transformer.executeChildTemplates(elem, true);
        }
        finally {
            transformer.setParameter(CURRENT_PORTAL_URL, Arrays.asList((Object)null));
        }
    }
    
    /**
     * Utility to retrieve the current portal URL from the transformer. To be used by elements that are supposed to
     * be nested inside of a url element.
     */
    protected T getCurrentPortalUrl(TransformerImpl transformer) {
        final List<?> urlHolder = (List<?>)transformer.getParameter(CURRENT_PORTAL_URL);
        if (urlHolder == null || urlHolder.size() == 0) {
            throw new IllegalStateException("There is no '" + this.expectedUrlType.getName() + "' available in the transformer");
        }
        return this.expectedUrlType.cast(urlHolder.get(0));
    }
}
