/**
 * Licensed to Apereo under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Apereo licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License.  You may obtain a
 * copy of the License at the following location:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.tenants;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jasig.portal.tenants.TenantOperationResponse.Result;

/**
 * Useful for extra user input validation in Create and Update tenant operations.
 *
 * @since 4.3
 * @author awills
 */
public class AttributeValidatingOperationsListener extends AbstractTenantOperationsListener {

    protected AttributeValidatingOperationsListener() {
        super("attribute-validating");
    }

    private Set<String> requiredAttributes;

    public void setRequiredAttributes(Set<String> requiredAttributes) {
        this.requiredAttributes = requiredAttributes;
    }

    @Override
    public void validateAttribute(final String key, final String value) throws Exception {
        if (requiredAttributes.contains(key)) {
            if (StringUtils.isBlank(value)) {
                throw new IllegalArgumentException("Missing required attribute:  " + key);
            }
        }
    }

}
