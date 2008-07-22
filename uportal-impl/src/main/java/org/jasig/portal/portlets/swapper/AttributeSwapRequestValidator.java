/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

import java.util.Map;
import java.util.Set;

import org.jasig.portal.portlets.Attribute;
import org.springframework.binding.message.MessageBuilder;
import org.springframework.binding.message.MessageContext;
import org.springframework.binding.message.MessageResolver;
import org.springframework.webflow.context.ExternalContext;
import org.springframework.webflow.execution.RequestContext;
import org.springframework.webflow.execution.RequestContextHolder;

/**
 * Validator for {@link PersonQuery}
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AttributeSwapRequestValidator {
    private IAttributeSwapperHelper attributeSwapperHelper;
    
    public IAttributeSwapperHelper getAttributeSwapperHelper() {
        return attributeSwapperHelper;
    }
    /**
     * {@link IAttributeSwapperHelper} to use for getting configuration and query information from for validation.
     */
    public void setAttributeSwapperHelper(IAttributeSwapperHelper attributeSwapperHelper) {
        this.attributeSwapperHelper = attributeSwapperHelper;
    }

    /**
     * Ensures all passed attributes are part of the valid query attribute set.
     */
    public void validateAttributesForm(AttributeSwapRequest attributeSwapRequest, MessageContext context) {
        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        final ExternalContext externalContext = requestContext.getExternalContext();
        final Set<String> swappableAttributes = this.attributeSwapperHelper.getSwappableAttributes(externalContext);
        
        final Map<String, Attribute> currentAttributes = attributeSwapRequest.getCurrentAttributes();
        this.checkAttributesMap(context, "currentAttributes", swappableAttributes, currentAttributes);

        final Map<String, Attribute> attributesToCopy = attributeSwapRequest.getAttributesToCopy();
        this.checkAttributesMap(context, "attributesToCopy", swappableAttributes, attributesToCopy);
    }

    /**
     * Checks the keys in the Map on the model against a Set to ensure there are no values in the Map that aren't also in the Set 
     */
    protected void checkAttributesMap(MessageContext context, String basePath, Set<String> swappableAttributes, Map<String, Attribute> attributesToCopy) {
        for (final String attribute : attributesToCopy.keySet()) {
            if (!swappableAttributes.contains(attribute)) {
                final MessageBuilder messageBuilder = new MessageBuilder();
                messageBuilder.error();
                messageBuilder.source(basePath + "[" + attribute + "].value");
                messageBuilder.code("attributesForm.invalidSwapAttribute"); 
                messageBuilder.arg(attribute);

                final MessageResolver errorMessage = messageBuilder.build();
                context.addMessage(errorMessage);
            }
        }
    }
}
