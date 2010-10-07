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

package org.jasig.portal.portlet.dao.jpa;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.Parameter;
import org.jasig.portal.portlet.om.IPortletPreference;
import org.jasig.portal.portlet.om.IPortletPreferences;

/**
 * @author Eric Dalquist
 * @version $Revision$
 */
@Entity
@Table(name = "UP_PORTLET_PREFS")
@GenericGenerator(
        name = "UP_PORTLET_PREFS_GEN", 
        strategy = "native", 
        parameters = {
            @Parameter(name = "sequence", value = "UP_PORTLET_PREFS_SEQ"),
            @Parameter(name = "table", value = "UP_JPA_UNIQUE_KEY"),
            @Parameter(name = "column", value = "NEXT_UP_PORTLET_PREFS_HI")
        }
    )
public class PortletPreferencesImpl implements IPortletPreferences {
    @Id
    @GeneratedValue(generator = "UP_PORTLET_PREFS_GEN")
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
     * @see org.jasig.portal.portlet.om.IPortletPreferences#replacePortletPreferences(java.util.List)
     */
    public void setPortletPreferences(List<IPortletPreference> newPreferences) {
        if (this.portletPreferences == newPreferences) {
            return;
        }
        
        Validate.notNull(newPreferences);
        
        if (this.portletPreferences == null) {
            this.portletPreferences = newPreferences;
        }
        else {
            //Build map of existing preferences for tracking which preferences have been removed
            final Map<String, IPortletPreference> oldPreferences = new LinkedHashMap<String, IPortletPreference>();
            for (final IPortletPreference preference : this.portletPreferences) {
                oldPreferences.put(preference.getName(), preference);
            }
            this.portletPreferences.clear();

            for (final IPortletPreference preference : newPreferences) {
                final String name = preference.getName();

                //Remove the existing preference from the map since it is supposed to be persisted 
                final IPortletPreference existingPreference = oldPreferences.remove(name);
                if (existingPreference == null) {
                    //New preference, add it to the list
                    this.portletPreferences.add(preference);
                }
                else {
                    //Existing preference, update the fields
                    existingPreference.setValues(preference.getValues());
                    existingPreference.setReadOnly(preference.isReadOnly());
                    this.portletPreferences.add(existingPreference);
                }
            }
        }
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
