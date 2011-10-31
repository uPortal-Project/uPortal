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

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;
import javax.persistence.Version;

import org.apache.commons.lang.builder.CompareToBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import org.jasig.portal.permission.IPermissionActivity;

/**
 * PermissionActivityImpl represents the default JPA implementation of 
 * IPermissionActivity.
 * 
 * @author Jen Bourey, jbourey@unicon.net
 * @version $Revision$
 * @since 3.3
 */
@Entity
@Table(name = "UP_PERMISSION_ACTIVITY")
@SequenceGenerator(
        name="UP_PERMISSION_ACTIVITY_GEN",
        sequenceName="UP_PERMISSION_ACTIVITY_SEQ",
        allocationSize=5
    )
@TableGenerator(
        name="UP_PERMISSION_ACTIVITY_GEN",
        pkColumnValue="UP_PERMISSION_ACTIVITY",
        allocationSize=5
    )
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
class PermissionActivityImpl implements IPermissionActivity, Serializable {
    
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_PERMISSION_ACTIVITY_GEN")
    @Column(name = "ACTIVITY_ID")
    private final long id;
    
    @Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;
    
    @Column(name = "ACTIVITY_FNAME", length = 128, nullable = false, unique = true)
	@Type(type = "fname")
    private String fname;
    
    @Column(name = "ACTIVITY_NAME", length = 128, nullable = false, unique = true)
    private String name;
    
    @Column(name = "ACTIVITY_DESCRIPTION", length = 255)
    private String description;

    @Column(name = "OWNER_TARGET_PROVIDER", length = 255, nullable = false)
    private String targetProviderKey;
    
    /*
     * Internal, for hibernate
     */
    @SuppressWarnings("unused")
    private PermissionActivityImpl() {
        this.id = -1;
        this.entityVersion = -1;
    }

    public PermissionActivityImpl(String name, String fname, String targetProviderKey) {
        this.id = -1;
        this.entityVersion = -1;
        this.name = name;
        this.fname = fname;
        this.targetProviderKey = targetProviderKey;
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

    @Override
    public String getTargetProviderKey() {
        return this.targetProviderKey;
    }

    @Override
    public void setTargetProviderKey(String targetProviderKey) {
        this.targetProviderKey = targetProviderKey;
    }

    @Override
    public Long getId() {
        return id;
    }

    /**
     * @see java.lang.Object#equals(Object)
     */
    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        }
        if (!(obj instanceof IPermissionActivity)) {
            return false;
        }

        IPermissionActivity activity = (IPermissionActivity) obj;
        return this.fname.equals(activity);
    }

    /**
     * @see java.lang.Object#hashCode()
     */
    @Override
    public int hashCode() {
        return new HashCodeBuilder(464270933, -1074792143).append(this.fname)
                .toHashCode();
    }

    @Override
    public String toString() {
        return "PermissionActivityImpl [id=" + this.id + ", entityVersion=" + this.entityVersion + ", fname="
                + this.fname + ", name=" + this.name + ", description=" + this.description + ", targetProviderKey="
                + this.targetProviderKey + "]";
    }

    @Override
    public int compareTo(IPermissionActivity activity) {
        return new CompareToBuilder()
                .append(this.name, activity.getName())
                .append(this.targetProviderKey, activity.getTargetProviderKey())
                .append(this.fname, activity.getFname())
                .toComparison();
    }
}
