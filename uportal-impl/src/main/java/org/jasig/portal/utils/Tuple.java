/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.utils;

import java.io.Serializable;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Simple object that contains two values who's references are immutable once initialized.
 * 
 * @author Eric Dalquist
 * @version $Revision$
 */
public class Tuple<A, B> implements Serializable {
    private static final long serialVersionUID = 1L;

    public final A first;
    public final B second;

    public Tuple(A first, B second) {
        this.first = first;
        this.second = second;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof Tuple)) {
            return false;
        }
        Tuple<?, ?> rhs = (Tuple<?, ?>) object;
        return new EqualsBuilder()
        .append(this.first, rhs.first)
        .append(this.second, rhs.second)
        .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(1423361201, 771516529)
            .append(this.first)
            .append(this.second)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("first", this.first)
            .append("second", this.second)
            .toString();
    }
}
