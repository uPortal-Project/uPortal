/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.portlets.lookup;

import java.util.Map;
import java.util.Set;

import org.jasig.portal.portlets.Attribute;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Autowired
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
