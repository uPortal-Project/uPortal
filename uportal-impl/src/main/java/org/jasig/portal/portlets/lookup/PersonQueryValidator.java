/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.lookup;

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
public class PersonQueryValidator {
    private IPersonLookupHelper personLookupHelper;
    
    public IPersonLookupHelper getPersonLookupHelper() {
        return personLookupHelper;
    }
    /**
     * {@link IPersonLookupHelper} to use for getting configuration and query information from for validation.
     */
    public void setPersonLookupHelper(IPersonLookupHelper personLookupHelper) {
        this.personLookupHelper = personLookupHelper;
    }


    /**
     * Ensures all passed attributes are part of the valid query attribute set.
     */
    public void validatePersonLookup(PersonQuery personQuery, MessageContext context) {
        final RequestContext requestContext = RequestContextHolder.getRequestContext();
        final ExternalContext externalContext = requestContext.getExternalContext();
        final Set<String> queryAttributes = personLookupHelper.getQueryAttributes(externalContext);
        
        final Map<String, Attribute> attributes = personQuery.getAttributes();
        for (final String attribute : attributes.keySet()) {
            if (!queryAttributes.contains(attribute)) {
                final MessageBuilder messageBuilder = new MessageBuilder();
                messageBuilder.error();
                messageBuilder.source("attributes[" + attribute + "].value");
                messageBuilder.code("personLookup.invalidQueryAttribute");
                messageBuilder.arg(attribute);

                final MessageResolver errorMessage = messageBuilder.build();
                context.addMessage(errorMessage);
            }
        }
    }
}
