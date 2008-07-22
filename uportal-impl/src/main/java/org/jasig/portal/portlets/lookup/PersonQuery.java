/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
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
