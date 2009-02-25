/**
 * Copyright (c) 2000-2009, Jasig, Inc.
 * See license distributed with this file and available online at
 * https://www.ja-sig.org/svn/jasig-parent/tags/rel-10/license-header.txt
 */
package org.jasig.portal.portlets;

import java.io.Serializable;

/**
 * Simple class that wraps a string and provides a getter/setter. Nessescary for binding into the value of a Map in 
 * WebFlow
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class Attribute implements Serializable {
    private static final long serialVersionUID = 1L;
    private String value = "";
    
    public Attribute() {
    }
    
    public Attribute(String value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public String getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(String value) {
        this.value = value;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#equals(java.lang.Object)
     */
    @Override
    public boolean equals(Object anObject) {
        return this.value == anObject || (this.value != null && this.value.equals(anObject));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return this.value != null ? this.value.hashCode() : 0;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return String.valueOf(this.value);
    }
}