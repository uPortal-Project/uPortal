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

package org.jasig.portal.portlets.swapper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.portlets.Attribute;
import org.jasig.portal.portlets.AttributeFactory;

/**
 * Request to change the specified attributes
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class AttributeSwapRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    private Map<String, Attribute> currentAttributes = LazyMap.decorate(new HashMap<String, Attribute>(), new AttributeFactory());
    @SuppressWarnings("unchecked")
    private Map<String, Attribute> attributesToCopy = LazyMap.decorate(new HashMap<String, Attribute>(), new AttributeFactory());

    /**
     * @return the currentAttributes
     */
    public Map<String, Attribute> getCurrentAttributes() {
        return this.currentAttributes;
    }
    /**
     * @param currentAttributes the currentAttributes to set
     */
    public void setCurrentAttributes(Map<String, Attribute> attributes) {
        if (attributes == null) {
            this.currentAttributes = new HashMap<String, Attribute>();
        }
        else {
            this.currentAttributes = attributes;
        }
    }
    
    /**
     * @return the attributesToCopy
     */
    public Map<String, Attribute> getAttributesToCopy() {
        return this.attributesToCopy;
    }
    /**
     * @param attributesToCopy the attributesToCopy to set
     */
    public void setAttributesToCopy(Map<String, Attribute> attributesToCopy) {
        if (attributesToCopy == null) {
            this.attributesToCopy = new HashMap<String, Attribute>();
        }
        else {
            this.attributesToCopy = attributesToCopy;
        }
    }
    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("currentAttributes", this.currentAttributes)
            .append("attributesToCopy", this.attributesToCopy)
            .toString();
    }
}
