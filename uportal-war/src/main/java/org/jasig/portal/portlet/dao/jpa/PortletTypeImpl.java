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

import java.io.Serializable;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.jasig.portal.portlet.om.IPortletDefinition;
import org.jasig.portal.portlet.om.IPortletType;

/**
 * JPA implementation of the IPortletType interface.
 * 
 * @author Ken Weiner, kweiner@unicon.net
 * @author Jen Bourey, jennifer.bourey@gmail.com
 * @version $Revision$
 */
@Entity
@Table(name = "UP_PORTLET_TYPE")
@SequenceGenerator(
        name="UP_PORTLET_TYPE_GEN",
        sequenceName="UP_PORTLET_TYPE_SEQ",
        allocationSize=1
    )
@TableGenerator(
        name="UP_PORTLET_TYPE_GEN",
        pkColumnValue="UP_PORTLET_TYPE",
        allocationSize=1
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
public class PortletTypeImpl implements Serializable, IPortletType {
    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(generator = "UP_PORTLET_TYPE_GEN")
	@Column(name = "TYPE_ID")
	private final int internalId;
    
    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;
    
    //Hidden reference to the child portlet definitions, used to allow cascading deletes where when a portlet type is deleted all associated definitions are also deleted
    //MUST BE LAZY FETCH, this set should never actually be populated at runtime or performance will be TERRIBLE
    @SuppressWarnings("unused")
    @OneToMany(mappedBy = "portletType", targetEntity = PortletDefinitionImpl.class, cascade = { CascadeType.ALL }, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<IPortletDefinition> portletDefinitions = null;


    @Column(name = "TYPE_NAME", length = 70, unique = true, nullable = false)
    private final String name;

	@Column(name = "TYPE_DESCR", length = 2000)
	private String descr;

	@Column(name = "TYPE_DEF_URI", length = 255, nullable = false)
	private String cpdUri;
	
	/**
	 * Default constructor used by hibernate
	 */
	@SuppressWarnings("unused")
    private PortletTypeImpl() {
	    this.internalId = -1;
	    this.entityVersion = -1;
        this.name = null;
	}
	

    public PortletTypeImpl(String name, String cpdUri) {
        this.internalId = -1;
        this.entityVersion = -1;
        this.name = name;
        this.cpdUri = cpdUri;
    }




    // Getter methods
	
	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IPortletType#getId()
	 */
	@Override
    public int getId() {
		return this.internalId;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IPortletType#getName()
	 */
	@Override
    public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IPortletType#getDescription()
	 */
	@Override
    public String getDescription() {
		return descr;
	}

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IPortletType#getCpdUri()
	 */
	@Override
    public String getCpdUri() {
		return cpdUri;
	}

	// Setter methods

	/*
	 * (non-Javadoc)
	 * @see org.jasig.portal.IPortletType#setDescription(java.lang.String)
	 */
	@Override
    public void setDescription(String descr) {
		this.descr = descr;
	}

    /* (non-Javadoc)
     * @see org.jasig.portal.channel.IPortletType#setCpdUri(java.lang.String)
     */
    @Override
    public void setCpdUri(String cpdUri) {
        this.cpdUri = cpdUri;
    }



    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object object) {
        if (object == this) {
            return true;
        }
        if (!(object instanceof IPortletType)) {
            return false;
        }
        IPortletType rhs = (IPortletType) object;
        return new EqualsBuilder()
            .append(this.name, rhs.getName())
            .append(this.cpdUri, rhs.getCpdUri())
            .isEquals();
    }


    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(-1497407419, 1799845985)
            .append(this.cpdUri)
            .append(this.name)
            .toHashCode();
    }


    @Override
    public String toString() {
        return "PortletTypeImpl [internalId=" + this.internalId + ", entityVersion=" + this.entityVersion + ", name="
                + this.name + ", descr=" + this.descr + ", cpdUri=" + this.cpdUri + "]";
    }
	
}
