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

package org.jasig.portal.permission.dao.jpa;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Cacheable;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.Type;
import org.jasig.portal.permission.IPermissionActivity;
import org.jasig.portal.permission.IPermissionOwner;

/**
 * PermissionOwnerImpl represents the default JPA implementation of 
 * IPermissionOwner.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
@Entity
@Table(name = "UP_PERMISSION_OWNER")
@SequenceGenerator(
        name="UP_PERMISSION_OWNER_GEN",
        sequenceName="UP_PERMISSION_OWNER_SEQ",
        allocationSize=1
    )
@TableGenerator(
        name="UP_PERMISSION_OWNER_GEN",
        pkColumnValue="UP_PERMISSION_OWNER",
        allocationSize=1
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class PermissionOwnerImpl implements IPermissionOwner, Serializable {

    private static final long serialVersionUID = 1L;

    @Id
	@GeneratedValue(generator = "UP_PERMISSION_OWNER_GEN")
    @Column(name = "OWNER_ID")
	private final Long id;
	
    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;
    
    @NaturalId
    @Column(name = "OWNER_FNAME", length = 128, nullable = false)
	@Type(type = "fname")
    private String fname;
    
    @Column(name = "OWNER_NAME", length = 128, nullable = false)
    private String name;
    
    @Column(name = "OWNER_DESCRIPTION", length = 255)
    private String description;

    @OneToMany(targetEntity = PermissionActivityImpl.class, fetch = FetchType.EAGER, cascade = { CascadeType.ALL }, orphanRemoval=true)
    @JoinColumn(name = "OWNER_ID")
    @Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
    @Fetch(FetchMode.JOIN)
    private Set<IPermissionActivity> activities = new HashSet<IPermissionActivity>();

    //For use by hibernate reflection
    @SuppressWarnings("unused")
    private PermissionOwnerImpl() {
        this.id = -1l;
        this.entityVersion = -1;
    }
    
    public PermissionOwnerImpl(String name, String fname) {
        this.id = -1l;
        this.entityVersion = -1;
        this.fname = fname;
        this.name = name;
    }



    @Override
    public Long getId() {
		return this.id;
	}

	@Override
    public String getFname() {
		return this.fname;
	}

	@Override
    public void setFname(String fname) {
		this.fname = fname;
	}

	@Override
    public String getName() {
		return this.name;
	}

	@Override
    public void setName(String name) {
		this.name = name;
	}

	@Override
    public String getDescription() {
		return this.description;
	}

	@Override
    public void setDescription(String description) {
		this.description = description;
	}

    public String getOwnerName() {
        return this.name;
    }

    public String getOwnerToken() {
        return this.fname;
    }

    @Override
    public Set<IPermissionActivity> getActivities() {
        return this.activities;
    }

    @Override
    public void setActivities(Set<IPermissionActivity> activities) {
        this.activities = activities;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IPermissionOwner)) {
            return false;
        }

        IPermissionOwner owner = (IPermissionOwner) obj;
        return this.fname.equals(owner.getFname());
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.fname == null) ? 0 : this.fname.hashCode());
        return result;
    }

    @Override
    public String toString() {
        return "PermissionOwnerImpl [id=" + this.id + ", entityVersion=" + this.entityVersion + ", fname=" + this.fname
                + ", name=" + this.name + ", description=" + this.description + "]";
    }
}
