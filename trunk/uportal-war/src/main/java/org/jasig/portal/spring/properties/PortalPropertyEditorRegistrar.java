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

package org.jasig.portal.spring.properties;

import java.beans.PropertyEditor;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.PropertyEditorRegistrar;
import org.springframework.beans.PropertyEditorRegistry;

/**
 * Registers a Map of PropertyEditors with the specified registry.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PortalPropertyEditorRegistrar implements PropertyEditorRegistrar {
    protected final Log logger = LogFactory.getLog(this.getClass());
    
    private Map<Class<?>, PropertyEditor> propertyEditors;
    
    /**
     * @return the propertyEditors
     */
    public Map<Class<?>, PropertyEditor> getPropertyEditors() {
        return propertyEditors;
    }
    /**
     * @param propertyEditors the propertyEditors to set
     */
    public void setPropertyEditors(Map<Class<?>, PropertyEditor> propertyEditors) {
        this.propertyEditors = propertyEditors;
    }


    /* (non-Javadoc)
     * @see org.springframework.beans.PropertyEditorRegistrar#registerCustomEditors(org.springframework.beans.PropertyEditorRegistry)
     */
    @Override
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        if (this.propertyEditors == null) {
            this.logger.warn("No PropertyEditors Map configured, returning with no action taken.");
            return;
        }
        
        for (final Map.Entry<Class<?>, PropertyEditor> editorEntry : this.propertyEditors.entrySet()) {
            final Class<?> requiredType = editorEntry.getKey();
            final PropertyEditor editor = editorEntry.getValue();
            
            if (this.logger.isTraceEnabled()) {
                this.logger.trace("Registering PropertyEditor '" + editor + "' for type '" + requiredType + "'");
            }
            
            registry.registerCustomEditor(requiredType, editor);
        }
    }
}
