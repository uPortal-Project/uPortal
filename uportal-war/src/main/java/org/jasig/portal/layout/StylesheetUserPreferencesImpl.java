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

package org.jasig.portal.layout;

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.commons.lang.Validate;
import org.jasig.portal.layout.om.IStylesheetUserPreferences;
import org.jasig.portal.utils.Populator;

/**
 * Stylesheet user preferences setup for memory or serialized storage. All APIs and returned objects are thread safe.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class StylesheetUserPreferencesImpl implements IStylesheetUserPreferences, Serializable {
    private static final long serialVersionUID = 1L;
    
    private final long stylesheetDescriptorId;
    private final int userId;
    private final int profileId;
    private final ConcurrentMap<String, String> outputProperties = new ConcurrentHashMap<String, String>();
    private final ConcurrentMap<String, String> parameters = new ConcurrentHashMap<String, String>();
    //NodeId -> Name -> Value
    private final ConcurrentMap<String, ConcurrentMap<String, String>> layoutAttributes = new ConcurrentHashMap<String, ConcurrentMap<String,String>>();
    
    public StylesheetUserPreferencesImpl(long stylesheetDescriptorId, int userId, int profileId) {
        this.stylesheetDescriptorId = stylesheetDescriptorId;
        this.userId = userId;
        this.profileId = profileId;
    }

    @Override
    public long getId() {
        return -1;
    }
    
    @Override
    public long getStylesheetDescriptorId() {
        return this.stylesheetDescriptorId;
    }

    @Override
    public int getUserId() {
        return this.userId;
    }

    @Override
    public int getProfileId() {
        return this.profileId;
    }

    @Override
    public String getOutputProperty(String name) {
        Validate.notEmpty(name, "name cannot be null");
        
        return this.outputProperties.get(name);
    }

    @Override
    public String setOutputProperty(String name, String value) {
        Validate.notEmpty(name, "name cannot be null");
        Validate.notEmpty(value, "value cannot be null");
        
        return this.outputProperties.put(name, value);
    }
    
    @Override
    public String removeOutputProperty(String name) {
        Validate.notEmpty(name, "name cannot be null");
        
        return this.outputProperties.remove(name);
    }

    @Override
    public <P extends Populator<String, String>> P populateOutputProperties(P properties) {
        properties.putAll(this.outputProperties);
        return properties;
    }
    
    @Override
    public void clearOutputProperties() {
        this.outputProperties.clear();
    }

    @Override
    public String getStylesheetParameter(String name) {
        Validate.notEmpty(name, "name cannot be null");
        
        return this.parameters.get(name);
    }

    @Override
    public String setStylesheetParameter(String name, String value) {
        Validate.notEmpty(name, "name cannot be null");
        Validate.notEmpty(value, "value cannot be null");
        
        return this.parameters.put(name, value);
    }
    
    @Override
    public String removeStylesheetParameter(String name) {
        Validate.notEmpty(name, "name cannot be null");
        
        return this.parameters.remove(name);
    }
    
    @Override
    public <P extends Populator<String, String>> P populateStylesheetParameters(P stylesheetParameters) {
        stylesheetParameters.putAll(this.parameters);
        return stylesheetParameters;
    }
    
    @Override
    public void clearStylesheetParameters() {
        this.parameters.clear();
    }

    @Override
    public String getLayoutAttribute(String nodeId, String name) {
        Validate.notEmpty(nodeId, "nodeId cannot be null");
        Validate.notEmpty(name, "name cannot be null");
        
        final Map<String, String> nodeAttributes = this.layoutAttributes.get(nodeId);
        if (nodeAttributes == null) {
            return null;
        }
        
        return nodeAttributes.get(name);
    }

    @Override
    public String setLayoutAttribute(String nodeId, String name, String value) {
        Validate.notEmpty(nodeId, "nodeId cannot be null");
        Validate.notEmpty(name, "name cannot be null");
        Validate.notEmpty(value, "value cannot be null");
        
        ConcurrentMap<String, String> nodeAttributes;
        synchronized (this.layoutAttributes) {
            nodeAttributes = this.layoutAttributes.get(nodeId);
            if (nodeAttributes == null) {
                nodeAttributes = new ConcurrentHashMap<String, String>();
                this.layoutAttributes.put(nodeId, nodeAttributes);
            }
        }
        
        return nodeAttributes.put(name, value);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.layout.om.IStylesheetUserPreferences#removeLayoutAttribute(java.lang.String, java.lang.String)
     */
    @Override
    public String removeLayoutAttribute(String nodeId, String name) {
        Validate.notEmpty(nodeId, "nodeId cannot be null");
        Validate.notEmpty(name, "name cannot be null");
        
        final Map<String, String> nodeAttributes = this.layoutAttributes.get(nodeId);
        if (nodeAttributes == null) {
            return null;
        }
        
        return nodeAttributes.remove(name);
    }

    @Override
    public <P extends Populator<String, String>> P populateLayoutAttributes(String nodeId, P layoutAttributes) {
        Validate.notEmpty(nodeId, "nodeId cannot be null");
        
        final Map<String, String> nodeAttributes = this.layoutAttributes.get(nodeId);
        if (nodeAttributes != null) {
            layoutAttributes.putAll(nodeAttributes);
        }
        
        return layoutAttributes;
    }
    
    @Override
    public Collection<String> getAllLayoutAttributeNodeIds() {
        return Collections.unmodifiableSet(this.layoutAttributes.keySet());
    }
    
    @Override
    public void clearLayoutAttributes(String nodeId) {
        Validate.notEmpty(nodeId, "nodeId cannot be null");
        
        this.layoutAttributes.remove(nodeId);
    }

    @Override
    public void clearAllLayoutAttributes() {
        synchronized (this.layoutAttributes) {
            this.layoutAttributes.clear();
        }
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.layoutAttributes == null) ? 0 : this.layoutAttributes.hashCode());
        result = prime * result + ((this.outputProperties == null) ? 0 : this.outputProperties.hashCode());
        result = prime * result + ((this.parameters == null) ? 0 : this.parameters.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        StylesheetUserPreferencesImpl other = (StylesheetUserPreferencesImpl) obj;
        if (this.layoutAttributes == null) {
            if (other.layoutAttributes != null)
                return false;
        }
        else if (!this.layoutAttributes.equals(other.layoutAttributes))
            return false;
        if (this.outputProperties == null) {
            if (other.outputProperties != null)
                return false;
        }
        else if (!this.outputProperties.equals(other.outputProperties))
            return false;
        if (this.parameters == null) {
            if (other.parameters != null)
                return false;
        }
        else if (!this.parameters.equals(other.parameters))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "StylesheetUserPreferencesImpl [outputProperties=" + this.outputProperties
                + ", parameters=" + this.parameters + ", layoutAttributes=" + this.layoutAttributes + "]";
    }
}
