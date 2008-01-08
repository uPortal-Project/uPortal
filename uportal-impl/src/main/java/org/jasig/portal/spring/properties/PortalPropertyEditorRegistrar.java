/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
    public void registerCustomEditors(PropertyEditorRegistry registry) {
        if (this.propertyEditors == null) {
            this.logger.warn("No PropertyEditors Map configured, returning with no action taken.");
            return;
        }
        
        for (final Map.Entry<Class<?>, PropertyEditor> editorEntry : this.propertyEditors.entrySet()) {
            final Class<?> requiredType = editorEntry.getKey();
            final PropertyEditor editor = editorEntry.getValue();
            
            if (this.logger.isDebugEnabled()) {
                this.logger.debug("Registering PropertyEditor '" + editor + "' for type '" + requiredType + "'");
            }
            
            registry.registerCustomEditor(requiredType, editor);
        }
    }
}
