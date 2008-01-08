/**
 * Copyright 2007 The JA-SIG Collaborative.  All rights reserved.
 * See license distributed with this file and
 * available online at http://www.uportal.org/license.html
 */
package org.jasig.portal.portlet.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.IndexColumn;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_PORTLET_PREFS")
class PortletPreferencesImpl implements IPortletPreferences {
    @SuppressWarnings("unused")
    @Id
    @GeneratedValue
    @Column(name = "PORTLET_PREFS_ID")
    private final long portletPreferencesId;
    
    @OneToMany(cascade = CascadeType.ALL, targetEntity = PortletPreferenceImpl.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "PORTLET_PREFS_ID")
    @IndexColumn(name = "PREF_ORDER")
    @Cascade( { org.hibernate.annotations.CascadeType.DELETE_ORPHAN, org.hibernate.annotations.CascadeType.ALL })
    private List<IPortletPreference> portletPreferences = new ArrayList<IPortletPreference>(0);
    
    
    public PortletPreferencesImpl() {
        this.portletPreferencesId = -1;
    }
    

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.prefs.IPortletPreferences#getPortletPreferences()
     */
    public List<IPortletPreference> getPortletPreferences() {
        return this.portletPreferences;
    }

    /* (non-Javadoc)
     * @see org.jasig.portal.om.portlet.prefs.IPortletPreferences#setPortletPreferences(java.util.List)
     */
    public void setPortletPreferences(List<IPortletPreference> portletPreferences) {
        Validate.notNull(portletPreferences);
        this.portletPreferences = portletPreferences;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPortletPreferences)) {
            return false;
        }
        IPortletPreferences rhs = (IPortletPreferences) object;
        return new EqualsBuilder()
            .append(this.portletPreferences, rhs.getPortletPreferences())
            .isEquals();
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-1904185833, -1222355625)
            .append(this.portletPreferences)
            .toHashCode();
    }

    /**
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.SHORT_PREFIX_STYLE)
            .append("portletPreferencesId", this.portletPreferencesId)
            .append("portletPreferences", this.portletPreferences)
            .toString();
    }
}
