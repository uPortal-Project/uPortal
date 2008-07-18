/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlets.swapper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.collections.Factory;
import org.apache.commons.collections.map.LazyMap;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
public class PersonQuery implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Attribute> attributes = new HashMap<String, Attribute>();

    /**
     * @return the attributes
     */
    public Map<String, Attribute> getAttributes() {
        return LazyMap.decorate(this.attributes, new Factory() {
            public Attribute create() {
                return new Attribute();
            }
        });
    }
    /**
     * @param attributes the attributes to set
     */
    public void setAttributes(Map<String, Attribute> attributes) {
        if (attributes == null) {
            this.attributes = new HashMap<String, Attribute>();
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
