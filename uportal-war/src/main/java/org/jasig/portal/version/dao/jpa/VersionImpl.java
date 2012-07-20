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
package org.jasig.portal.version.dao.jpa;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

import org.hibernate.annotations.NaturalId;
import org.hibernate.annotations.NaturalIdCache;
import org.hibernate.annotations.Type;
import org.jasig.portal.version.AbstractVersion;

@Entity
@Table(name = "UP_VERSION")
@SequenceGenerator(
        name="UP_VERSION_GEN",
        sequenceName="UP_VERSION_SEQ",
        allocationSize=1
    )
@TableGenerator(
        name="UP_VERSION_GEN",
        pkColumnValue="UP_VERSION",
        allocationSize=1
    )
@NaturalIdCache
class VersionImpl extends AbstractVersion {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(generator = "UP_VERSION_GEN")
    @Column(name = "VERSION_ID")
    private final long id;
    
    @javax.persistence.Version
    @Column(name = "ENTITY_VERSION")
    private final long entityVersion;
    
    @NaturalId
    @Column(name = "PRODUCT", length = 128, nullable = false)
    @Type(type = "fname")
    private final String product;
    
    @Column(name = "MAJOR_VER", nullable = false)
    private int major;

    @Column(name = "MINOR_VER", nullable = false)
    private int minor;
    
    @Column(name = "PATCH_VER", nullable = false)
    private int patch;
    
    @SuppressWarnings("unused")
    private VersionImpl() {
        this.id = -1;
        this.entityVersion = -1;
        this.product = null;
    }
    
    public VersionImpl(String product, int major, int minor, int patch) {
        this.id = -1;
        this.entityVersion = -1;
        this.product = product;
        this.major = major;
        this.minor = minor;
        this.patch = patch;
    }

    @Override
    public int getMajor() {
        return this.major;
    }

    @Override
    public int getMinor() {
        return this.minor;
    }

    @Override
    public int getPatch() {
        return this.patch;
    }
    
    public void setMajor(int major) {
        this.major = major;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public void setPatch(int patch) {
        this.patch = patch;
    }
}
