/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao.jpa;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.pluto.internal.InternalPortletPreference;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Type;
import org.jasig.portal.portlet.om.IPortletPreference;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(
        name = "UP_PORTLET_PREF", 
        uniqueConstraints = @UniqueConstraint(columnNames = { "NAME", "PORTLET_PREF_ID" })
)
public class PortletPreferenceImpl implements IPortletPreference {
    @SuppressWarnings("unused")
    @Id
    @GeneratedValue
    @Column(name = "PORTLET_PREF_ID")
    private final long portletPreferenceId;
    
    @Column(name = "NAME", nullable = false)
    @Type(type = "text")
    private String name = null;
    
    @Column(name = "READ_ONLY", nullable = false)
    private boolean readOnly = false;
    
    @CollectionOfElements
    @JoinTable(
        name = "UP_PORTLET_PREF_VALUES",
        joinColumns = @JoinColumn(name = "PORTLET_PREF_ID")
    )
    @IndexColumn(name="VALUE_ORDER")
    @Type(type = "text")
    @Column(name = "VALUE")
    private List<String> values = null;
    
    
    public PortletPreferenceImpl() {
        this.portletPreferenceId = -1;
    }
    
    public PortletPreferenceImpl(InternalPortletPreference portletPreference) {
        this.portletPreferenceId = -1;
        this.name = portletPreference.getName();
        this.readOnly = portletPreference.isReadOnly();
        this.setValues(portletPreference.getValues());
    }
    
    public PortletPreferenceImpl(String name, boolean readOnly, String... values) {
        this.portletPreferenceId = -1;
        this.name = name;
        this.readOnly = readOnly;
        this.setValues(values);
    }

    
    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.prefs.InternalPortletPreference#getName()
     */
    public String getName() {
        return this.name;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.prefs.InternalPortletPreference#getValues()
     */
    public String[] getValues() {
        return this.values.toArray(new String[this.values.size()]);
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.prefs.InternalPortletPreference#isReadOnly()
     */
    public boolean isReadOnly() {
        return this.readOnly;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.prefs.InternalPortletPreference#setValues(java.lang.String[])
     */
    public void setValues(String[] values) {
        this.values = new ArrayList<String>(Arrays.asList(values));
    }

    /* (non-Javadoc)
     * @see java.lang.Object#clone()
     */
    @Override
    public Object clone() {
        return new PortletPreferenceImpl(this);
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPortletPreference)) {
            return false;
        }
        IPortletPreference rhs = (IPortletPreference) object;
        return new EqualsBuilder()
            .append(this.name, rhs.getName())
            .append(this.readOnly, rhs.isReadOnly())
            .append(this.getValues(), rhs.getValues())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-1904185833, -1222355625)
            .append(this.name)
            .append(this.readOnly)
            .append(this.values)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("portletPreferenceId", this.portletPreferenceId)
            .append("name", this.name)
            .append("readOnly", this.readOnly)
            .append("values", this.values)
            .toString();
    }
}
