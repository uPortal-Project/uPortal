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
public class AttributeSwapRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Map<String, Attribute> currentAttributes = new HashMap<String, Attribute>();
    private Map<String, Attribute> attributesToCopy = new HashMap<String, Attribute>();

    /**
     * @return the currentAttributes
     */
    public Map<String, Attribute> getCurrentAttributes() {
        return LazyMap.decorate(this.currentAttributes, new Factory() {
            public Attribute create() {
                return new Attribute();
            }
        });
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
        return LazyMap.decorate(this.attributesToCopy, new Factory() {
            public Attribute create() {
                return new Attribute();
            }
        });
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
