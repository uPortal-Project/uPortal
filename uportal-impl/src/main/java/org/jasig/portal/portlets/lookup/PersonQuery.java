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

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jasig.portal.portlets.Attribute;
import org.jasig.portal.portlets.AttributeFactory;

/**
 * Represents a query for a person using attributes submitted by the user.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PersonQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    @SuppressWarnings("unchecked")
    private Map<String, Attribute> attributes = LazyMap.decorate(new HashMap<String, Attribute>(), new AttributeFactory());

    /**
     * @return the attributes
     */
    public Map<String, Attribute> getAttributes() {
        return this.attributes;
    }
    /**
     * @param attributes the attributes to set
     */
    @SuppressWarnings("unchecked")
    public void setAttributes(Map<String, Attribute> attributes) {
        if (attributes == null) {
            this.attributes = LazyMap.decorate(new HashMap<String, Attribute>(), new AttributeFactory());
        }
        else {
            this.attributes = attributes;
        }
    }


    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("attributes", this.attributes)
            .toString();
    }
}
