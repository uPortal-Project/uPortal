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

package org.jasig.portal.xml.xpath;

import java.util.Map;

import javax.xml.namespace.QName;
import javax.xml.xpath.XPathVariableResolver;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
class ThreadLocalXPathVariableResolver implements XPathVariableResolver {
    private final ThreadLocal<Map<String, ?>> localVariables = new ThreadLocal<Map<String, ?>>();
    
    public void setVariables(Map<String, ?> variables) {
        this.localVariables.set(variables);
    }
    
    public void clearVariables() {
        this.localVariables.set(null);
    }
    
    /* (non-Javadoc)
     * @see javax.xml.xpath.XPathVariableResolver#resolveVariable(javax.xml.namespace.String)
     */
    @Override
    public Object resolveVariable(QName variableName) {
        final Map<String, ?> variables = this.localVariables.get();
        if (variables == null) {
            return null;
        }
        
        final String localPart = variableName.getLocalPart();
        return variables.get(localPart);
    }
}
